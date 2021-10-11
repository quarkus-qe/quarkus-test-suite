package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class DefaultMinLogLevelIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void checkDefaultLogMinLevel() {
        app.given().when().get("/log").then().statusCode(204);

        app.logs().assertContains("Fatal log example");
        app.logs().assertContains("Error log example");
        app.logs().assertContains("Warn log example");
        app.logs().assertContains("Info log example");
        app.logs().assertContains("Debug log example");

        // the value of minimum logging level overrides the logging level
        app.logs().assertDoesNotContain("Trace log example");
    }
}
