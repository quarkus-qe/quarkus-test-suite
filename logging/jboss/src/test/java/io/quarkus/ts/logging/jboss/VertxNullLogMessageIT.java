package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6806")
@QuarkusScenario
public class VertxNullLogMessageIT {

    private static final String MISSING_LOG_MESSAGE = "<missing-log-message>";
    private static final String NULL_MESSAGE_JSON = "\"message\":\"NULL\"";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("default.properties");

    @Test
    public void verifyVertxLoggerDoesNotProduceNullStringForNullMessage() {
        app.given().post("/vertx-log/null-message");
        app.logs().assertContains(MISSING_LOG_MESSAGE);
        app.logs().assertDoesNotContain(NULL_MESSAGE_JSON);
    }
}
