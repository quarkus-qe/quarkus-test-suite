package io.quarkus.ts.security.form.authn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.form.authn.corsConfig.FullCorsConfig;
import io.quarkus.ts.security.form.authn.corsConfig.SimpleCorsConfig;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-6684")
public class CorsIT {
    public static final String ORIGIN_HEADER = "Origin";
    public static final String ALLOWED_ORIGIN = "http://localhost";
    public static final String DENIED_ORIGIN = "http://remotehost";

    // app with simple CORS config
    @QuarkusApplication(classes = { SimpleCorsConfig.class }, includeAllClassesFromMain = true)
    static RestService simpleapp = new RestService();

    // app with full CORS config
    @QuarkusApplication(classes = { FullCorsConfig.class }, includeAllClassesFromMain = true)
    static RestService fullapp = new RestService();

    @Test
    public void simpleAccessGrantedTest() {
        Response response = simpleapp.given()
                .header(ORIGIN_HEADER, ALLOWED_ORIGIN)
                .get("/public").andReturn();

        assertEquals(HttpStatus.SC_OK, response.statusCode(), "Correct Origin should get allowed access");
        assertEquals(ALLOWED_ORIGIN, response.header("access-control-allow-origin"),
                "HTTP response should contain header \"access-control-allow-origin\" with set value");
        assertEquals("public", response.body().asString(), "Response body should match expected text");
    }

    @Test
    public void simpleAccessDeniedTest() {
        simpleapp.given()
                .header(ORIGIN_HEADER, DENIED_ORIGIN)
                .get("/public")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void fullCorrectOriginTest() {
        Response response = fullapp.given()
                .header(ORIGIN_HEADER, ALLOWED_ORIGIN)
                .post("/public")
                .andReturn();

        assertEquals(HttpStatus.SC_OK, response.statusCode(), "Correct Origin should get allowed access");
        assertEquals(ALLOWED_ORIGIN, response.header("access-control-allow-origin"),
                "HTTP response should contain header \"access-control-allow-origin\" with set value");
        assertEquals("post", response.body().asString(), "Response body should match expected text");
    }

    @Test
    public void fullInvalidMethodTest() {
        // only POST method is allowed in FullCorsConfig
        fullapp.given()
                .header(ORIGIN_HEADER, ALLOWED_ORIGIN)
                .get("/public")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void fullInvalidOriginTest() {
        fullapp.given()
                .header(ORIGIN_HEADER, DENIED_ORIGIN)
                .post("/public")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
