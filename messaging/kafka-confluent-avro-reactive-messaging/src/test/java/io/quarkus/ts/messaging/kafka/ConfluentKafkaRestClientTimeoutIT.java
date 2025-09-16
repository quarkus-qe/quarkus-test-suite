package io.quarkus.ts.messaging.kafka;

import static org.apache.http.HttpStatus.SC_OK;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.quarkus.ts.messaging.kafka.restclient.SignalRestResource;
import io.quarkus.ts.messaging.kafka.restclient.UppercaseRestClient;
import io.quarkus.ts.messaging.kafka.restclient.UppercaseRestResource;

@QuarkusScenario
@Tag("podman-incompatible") //todo fails on podman 4.4.1, works on 4.5.0 and above
@Tag("https://github.com/quarkusio/quarkus/issues/48983")
public class ConfluentKafkaRestClientTimeoutIT {

    private static final int ASSERT_TIMEOUT_SECONDS = 40;

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT, withRegistry = true)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication(classes = { UppercaseRestClient.class, UppercaseRestResource.class,
            SignalRestResource.class }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(artifactId = "quarkus-rest-client-jackson"),
                    @Dependency(artifactId = "quarkus-smallrye-fault-tolerance")
            })
    static final RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("confluent.registry.url", kafka::getRegistryUrl);

    @Test
    void testKafkaConsumerCallsRestClient() {
        String uuid = app.given()
                .get("/signal")
                .then()
                .statusCode(SC_OK)
                .extract()
                .asString();

        await().atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
            app.given()
                    .get("/signal/last")
                    .then()
                    .statusCode(SC_OK)
                    .body(equalTo(uuid.toUpperCase()));
        });
    }
}
