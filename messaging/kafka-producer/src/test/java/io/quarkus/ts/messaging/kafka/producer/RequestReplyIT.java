package io.quarkus.ts.messaging.kafka.producer;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
public class RequestReplyIT {
    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication(classes = { KafkaRequestReplyEmitter.class, KafkaRequestReplyProcessor.class })
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            // wait for kafka to properly set up the topics, before running tests
            // if not waited for, the connection to kafka will fail
            .onPostStart(event -> kafka.logs().assertContains("Stabilized group kafka-producer"));

    @Test
    public void kafkaRequestReplyTest() {
        String request = "Hello World!";
        app.given()
                .param("request", request)
                .get("/request")
                .then()
                .statusCode(SC_OK)
                .body(is(request.toUpperCase()));
    }

    @Test
    public void kafkaRequestReplyMessageTest() {
        String request = "Hello message!";
        app.given()
                .param("request", request)
                .get("/request/message")
                .then()
                .statusCode(SC_OK)
                .body(is(request.toLowerCase()));
    }
}
