package io.quarkus.ts.stork;

import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication(classes = {
            IGreetingResource.class, GreetingResource.class, PriceConsumer.class, KafkaPriceProducer.class
    })
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void testNoStorkNPEOnGracefulShutdown_LogCheck() throws InterruptedException {
        String npeStringPart1 = "Stork.getInstance()\" is null";
        String npeStringPart2 = "StorkClientRequestFilter";
        long initialRunDurationMillis = 5000;
        boolean storkNpeFound = false;
        Thread.sleep(initialRunDurationMillis);
        app.stop();
        testLog.info("Application shutdown completed.");
        List<String> appLogs = app.getLogs();

        for (String logLine : appLogs) {
            if (logLine.contains(npeStringPart1) && logLine.contains(npeStringPart2)) {
                testLog.error("Stork NPE String FOUND! in application logs: " + logLine);
                storkNpeFound = true;
                break;
            }
        }
        assertFalse(storkNpeFound,
                "The Stork NPE related to 'Stork.getInstance() is null' in 'StorkClientRequestFilter' " +
                        "should NOT be present in the application logs after graceful shutdown with the fix.");
    }
}
