package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.PremierLeagueContainerRequestFilter.REQ_PARAM_NAME;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;

@Tag("QUARKUS-1546")
@QuarkusScenario
public class ContainerRequestFilterReactiveIT {
    // DROPME just to check latest framework is really used
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false")
            .withProperty("pl-container-request-filter.enabled", "true");

    @Test
    void testReqFilterIsAppliedAndReqSucceed() {
        RestAssured
                .given()
                .queryParam(REQ_PARAM_NAME, "CR7")
                .get("/api/hello")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void testReqFilterIsAppliedAndReqDenied() {
        RestAssured
                .given()
                .get("/api/hello")
                .then()
                .statusCode(HttpStatus.SC_CONFLICT);
    }

}
