package io.quarkus.ts.external.applications;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support long file paths")
public class QuickstartIT {
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-quickstarts.git", contextDir = "getting-started")
    static final RestService app = new RestService();

    @Test
    public void verify() {
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
