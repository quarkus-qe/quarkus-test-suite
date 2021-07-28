package io.quarkus.ts.memoryLeaks;

import static io.restassured.RestAssured.given;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

import io.restassured.http.ContentType;

/**
 * We understand memory leaks as objects present in the heap that are no longer used,
 * but the garbage collector is unable to remove. IF heap memory increase their value over the time
 * and is always greater than the first round after the warming up iterations then you probably have a memory leak.
 */
public class MemoryManager {

    static final Logger LOG = Logger.getLogger(MemoryManager.class.getName());
    static final String WARMING_ITERATIONS_NAME = "ts.long-running.warming-up.iterations";
    static final String WARMING_ITERATIONS_DEFAULT_VALUE = "1";
    static final String MEMORY_LEAK_THRESHOLD_NAME = "ts.long-running.memory-leak.threshold.percentage";
    static final String MEMORY_LEAK_THRESHOLD_DEFAULT_VALUE = "15";
    static final String GC_AWAIT_SEC_NAME = "ts.long-running.garbage-collector.await.sec";
    static final String GC_AWAIT_SEC_DEFAULT_VALUE = "120";

    static final Pattern OLD_JVM_HEAP_REGEXP = Pattern.compile("(jvm.memory.used;area=heap;id=).*(Old Gen)");
    static final Pattern SURVIVOR_JVM_REGEXP = Pattern.compile("(jvm.memory.used;area=heap;id=).*(Survivor Space)");
    static final Pattern EDEN_JVM_REGEXP = Pattern.compile("(jvm.memory.used;area=heap;id=).*(Eden Space)");

    MemoryBucket memoryBucket = new MemoryBucket();
    MemoryBucket coolDownMemoryBucket = new MemoryBucket();

    int warmingUpIterations;
    int threshold;
    int garbageCollectorAwaitSec;

    public MemoryManager() {
        warmingUpIterations = Integer.parseInt(System.getProperty(WARMING_ITERATIONS_NAME, WARMING_ITERATIONS_DEFAULT_VALUE));
        threshold = Integer.parseInt(System.getProperty(MEMORY_LEAK_THRESHOLD_NAME, MEMORY_LEAK_THRESHOLD_DEFAULT_VALUE));
        garbageCollectorAwaitSec = Integer.parseInt(System.getProperty(GC_AWAIT_SEC_NAME, GC_AWAIT_SEC_DEFAULT_VALUE));
    }

    public void warmingUp(String path) {
        IntStream.range(0, warmingUpIterations).forEach(i -> given()
                .when().get(path)
                .then()
                .statusCode(HttpStatus.SC_OK));
    }

    public void pullJvmMemoryUsage() {
        Map jsonAsMap = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when().get("/q/metrics-json")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(Map.class);

        memoryBucket.upsert(totalHeapUsedMemory(jsonAsMap));
    }

    public void pullCoolDownJvmMemoryUsage() {
        Map jsonAsMap = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when().get("/q/metrics-json")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(Map.class);

        coolDownMemoryBucket.upsert(totalHeapUsedMemory(jsonAsMap));
    }

    public MemoryReport memoryLeaksReport() {
        return new MemoryReport(memoryBucket, coolDownMemoryBucket, threshold);
    }

    public void runGC() {
        try {
            LOG.info("Running garbage collector");
            System.gc();
            Thread.sleep(Duration.ofSeconds(garbageCollectorAwaitSec).toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long totalHeapUsedMemory(Map metricReport) {
        Number old = (Number) metricReport.get(getJvmMemoryKey(metricReport, OLD_JVM_HEAP_REGEXP));
        Number survivor = (Number) metricReport.get(getJvmMemoryKey(metricReport, SURVIVOR_JVM_REGEXP));
        Number eden = (Number) metricReport.get(getJvmMemoryKey(metricReport, EDEN_JVM_REGEXP));

        return old.longValue() + survivor.longValue() + eden.longValue();
    }

    private String getJvmMemoryKey(Map<String, Object> metricReport, Pattern keyExp) {
        return metricReport.keySet()
                .stream().filter(keyExp.asPredicate()).findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
