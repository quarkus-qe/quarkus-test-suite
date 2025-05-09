package io.quarkus.ts.stork;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import java.util.List;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@Tag("https://github.com/quarkusio/quarkus/issues/41658")
@QuarkusScenario
public class KafkaWithStorkIT {

    private static final Logger testLog = Logger.getLogger(KafkaWithStorkIT.class);
    private static final String STORK_GET_INSTANCE_NPE_SIGNATURE = "Cannot invoke \"io.smallrye.stork.Stork.getService(String)\" because the return value of \"io.smallrye.stork.Stork.getInstance()\" is null";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication(classes = {
            IGreetingResource.class, GreetingResource.class, PriceConsumer.class, KafkaPriceProducer.class
    })
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void testNoStorkNPEOnGracefulShutdown_LogCheck() {
        final int expectedMessageCount = 3;
        await().atMost(Duration.ofSeconds(25))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    List<String> logs = app.getLogs();
                    long count = logs.stream()
                            .filter(line -> line.contains("Received price"))
                            .count();
                    return count >= expectedMessageCount;
                });
        app.stop();
        testLog.info("Application shutdown completed.");
        boolean hasStorkNPE = app.getLogs().stream()
                .anyMatch(line -> line.contains(STORK_GET_INSTANCE_NPE_SIGNATURE));
        assertFalse(hasStorkNPE, "Specific NPE Stork.getInstance() should not come up during shutdown");
    }
}
