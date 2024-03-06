package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
public class LocalOptionsIT {

    @QuarkusApplication
    static final RestService app = new RestService();

    @QuarkusApplication
    static final RestService custom = new RestService()
            .withProperty("quarkus.management.port", "9002");

    @QuarkusApplication
    static final RestService tls = new RestService()
            .withProperty("quarkus.management.port", "9003")
            .withProperty("quarkus.management.ssl.certificate.key-store-file", "META-INF/resources/server.keystore")
            .withProperty("quarkus.management.ssl.certificate.key-store-password", "password")
            .withProperty("quarkus.management.ssl.certificate.key-store-file-type", "PKCS12");

    @QuarkusApplication
    static final RestService unmanaged = new RestService()
            .withProperty("quarkus.management.enabled", "false");

    @QuarkusApplication
    static final RestService redirected = new RestService()
            .withProperty("quarkus.management.port", "9004")
            .withProperty("quarkus.http.root-path", "/api")
            .withProperty("quarkus.http.non-application-root-path", "/query")
            .withProperty("quarkus.management.root-path", "management");

    @Test
    public void greeting() {
        for (RestService service : Arrays.asList(app, custom, tls, unmanaged)) {
            Response response = service.given().get("/ping");
            assertEquals(200, response.statusCode());
            assertEquals("pong", response.body().asString());

            Response openapi = service.given().get("/q/openapi?format=json"); //openapi should be on the old interface
            assertEquals(200, openapi.statusCode());
            JsonPath json = openapi.body().jsonPath();
            assertEquals("OK", json.getString("paths.\"/ping\".get.responses.\"200\".description"));
        }
    }

    @Test
    public void unmanaged() {
        unmanaged.given().get("q/health").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void managed() {
        app.management().get("q/health").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void customPort() {
        custom.management()
                .get("q/health").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void tls() {
        tls.management()
                .relaxedHTTPSValidation()
                .get("q/health").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void redirected() {
        Response response = redirected.given().get("api/ping");
        assertEquals(200, response.statusCode());
        assertEquals("pong", response.body().asString());

        redirected.management().get("management/health").then()
                .statusCode(HttpStatus.SC_OK);

        Response openapi = redirected.given().get("query/openapi?format=json");
        assertEquals(200, openapi.statusCode());
        JsonPath json = openapi.body().jsonPath();
        assertEquals("OK", json.getString("paths.\"/api/ping\".get.responses.\"200\".description"));
    }
}
