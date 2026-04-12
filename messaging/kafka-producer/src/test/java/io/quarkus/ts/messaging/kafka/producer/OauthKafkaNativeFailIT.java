package io.quarkus.ts.messaging.kafka.producer;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7308")
@EnabledOnNative
@QuarkusScenario
public class OauthKafkaNativeFailIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("oauthbearer.properties")
            .setAutoStart(false);

    @Test
    public void checkThatQuarkusStartNotFailWithMissingConstructor() {
        assertThrows(AssertionError.class, () -> app.start(),
                "Expecting to fail to start as it's need to set complicated setup for Kafka");
        app.logs().assertDoesNotContain(
                "Could not find a public no-argument constructor for org.apache.kafka.common.security.oauthbearer.DefaultJwtRetriever");
        app.logs().assertDoesNotContain(
                "Could not find a public no-argument constructor for org.apache.kafka.common.security.oauthbearer.DefaultJwtValidator");
        // Need to check if it failed with expected error
        app.logs().assertContains("configuration encountered an error on configure(): Invalid value");
    }
}
