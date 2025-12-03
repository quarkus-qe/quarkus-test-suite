package io.quarkus.ts.security.form.authn;

import static io.quarkus.ts.security.form.authn.csrf.CsrfConfiguration.COOKIE_NAME;
import static io.quarkus.ts.security.form.authn.csrf.CsrfConfiguration.FORM_FIELD_NAME;
import static io.quarkus.ts.security.form.authn.csrf.CsrfConfiguration.TOKEN_HEADER_NAME;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.form.authn.csrf.CsrfConfiguration;
import io.quarkus.ts.security.form.authn.csrf.CsrfResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class CsrfIT {
    @QuarkusApplication(classes = { CsrfConfiguration.class, CsrfResource.class }, dependencies = {
            @Dependency(artifactId = "quarkus-rest-csrf"), @Dependency(artifactId = "quarkus-security")
    })
    static RestService app = new RestService();

    @Test
    public void formParamTest() {
        Response response = given().get("/csrf/csrfTokenForm").andReturn();
        var cookies = response.cookies();
        assertTrue(cookies.containsKey(COOKIE_NAME), "CSRF should set cookie with name: " + COOKIE_NAME);
        assertFalse(cookies.get(COOKIE_NAME).isEmpty(), "CSRF cookie should not be empty");

        String token = cookies.get(COOKIE_NAME);
        given()
                .cookie(COOKIE_NAME, token)
                .param(FORM_FIELD_NAME, token)
                .formParam("name", "test")
                .contentType(ContentType.URLENC)
                .when()
                .post("/csrf/csrfTokenForm")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void withoutTokenTest() {
        given()
                .formParam("name", "test")
                .contentType(ContentType.URLENC)
                .when()
                .post("/csrf/csrfTokenForm")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void wrongTokenTest() {
        given()
                .cookie(COOKIE_NAME, "randomValue")
                .param(FORM_FIELD_NAME, "randomValue")
                .formParam("name", "test")
                .contentType(ContentType.URLENC)
                .when()
                .post("/csrf/csrfTokenForm")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void httpHeaderTest() {
        Response response = given().get("/csrf/csrfTokenForm").andReturn();
        var cookies = response.cookies();
        assertTrue(cookies.containsKey(COOKIE_NAME), "CSRF should set cookie with name: " + COOKIE_NAME);
        assertFalse(cookies.get(COOKIE_NAME).isEmpty(), "CSRF cookie should not be empty");

        String token = cookies.get(COOKIE_NAME);
        given()
                .cookie(COOKIE_NAME, token)
                .header(TOKEN_HEADER_NAME, token)
                .formParam("name", "test")
                .contentType(ContentType.URLENC)
                .when()
                .post("/csrf/csrfTokenForm")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
