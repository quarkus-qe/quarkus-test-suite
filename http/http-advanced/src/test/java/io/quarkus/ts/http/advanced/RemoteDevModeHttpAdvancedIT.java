package io.quarkus.ts.http.advanced;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.IsRunningCheck;
import io.quarkus.test.services.RemoteDevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.smallrye.common.os.OS;

@QuarkusScenario
public class RemoteDevModeHttpAdvancedIT {

    @RemoteDevModeQuarkusApplication(isRunningCheck = IsRunningCheck.IsBasePathReachableCheck.class)
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    @Test
    public void serviceShouldBeUpAndRunning() {
        // TODO: drop Windows workaround when https://github.com/quarkusio/quarkus/issues/49434 is fixed
        if (OS.WINDOWS.isCurrent()) {
            AwaitilityUtils.untilAsserted(
                    () -> given().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!")));
        } else {
            given().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
        }
    }

    @Tag("QUARKUS-834")
    @Test
    public void devUiShouldBeNotFound() {
        // TODO: drop Windows workaround when https://github.com/quarkusio/quarkus/issues/49434 is fixed
        if (OS.WINDOWS.isCurrent()) {
            AwaitilityUtils.untilAsserted(() -> app.given().get("/q/dev").then().statusCode(HttpStatus.SC_NOT_FOUND));
        } else {
            app.given().get("/q/dev").then().statusCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}
