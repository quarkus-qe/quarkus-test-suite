package io.quarkus.ts.http.advanced.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.RemoteDevModeQuarkusApplication;

@QuarkusScenario
public class RemoteDevModeHttpAdvancedReactiveIT {

    @RemoteDevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    @Test
    public void serviceShouldBeUpAndRunning() {
        given().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
    }

    @Test
    public void devUiShouldBeAvailable() {
        app.given().get("/q/dev-ui").then().statusCode(HttpStatus.SC_OK);
    }
}
