package io.quarkus.ts.security.form.authn;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

@Tag("QUARKUS-1540")
@QuarkusScenario
public class BasicAuthnIT {
    static final String USER_USERNAME = "isaac";
    static final String USER_PASSWORD = "N3wt0N";

    @QuarkusApplication
    static RestService app = new RestService().withProperties("basicAuthN.properties");

    @Test
    public void basicAuthDisabled() {
        basicAuthRequest("/user", USER_USERNAME, USER_PASSWORD).statusCode(HttpStatus.SC_FORBIDDEN);
    }

    private ValidatableResponse basicAuthRequest(String url, String username, String password) {
        RequestSpecification test = given();
        if (username != null && password != null) {
            test = test.auth().basic(username, password);
        }

        return test.when().get(url).then();
    }
}
