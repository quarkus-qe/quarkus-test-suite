package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Tests for Kafka and Metrics scenarios
 */
@QuarkusScenario
public class KafkaAndMetricsIT {
    @KafkaContainer
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService().withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    /**
     * Test to ensure Kafka version is visible in metrics, especially in native mode
     * Issues: https://github.com/quarkusio/quarkus/pull/41278 and https://github.com/quarkusio/quarkus/issues/42865
     */
    @Test
    public void testKafkaVersionInMetrics() {
        String metrics = app.given().when().get("/q/metrics").then().statusCode(200).extract().asString();

        boolean isKafkaVersionPresent = metrics.contains("kafka_version");
        boolean isKafkaVersionUnknown = metrics.contains("kafka_version=\"unknown\"");

        assertTrue(isKafkaVersionPresent, "'kafka_version' string is  not present in the metrics response");
        assertFalse(isKafkaVersionUnknown, "'kafka_version' is 'unknown' in the metrics response");
    }
}
