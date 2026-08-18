package io.quarkus.qe;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;

@Tag("QUARKUS-7819")
@QuarkusScenario
public class ManagementCorsIT {
    private static final String APP_ORIGIN = "http://localhost:3000";
    private static final String MANAGEMENT_ORIGIN = "http://localhost:3001";
    private static final String OTHER_ORIGIN = "http://localhost:3002";

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.http.cors.enabled", "true")
            .withProperty("quarkus.http.cors.origins", APP_ORIGIN)
            .withProperty("quarkus.management.cors.enabled", "true")
            .withProperty("quarkus.management.cors.origins", MANAGEMENT_ORIGIN);

    @QuarkusApplication
    static final RestService corsDisabled = new RestService()
            .withProperty("quarkus.management.port", "9002");

    @Test
    public void allowedOrigin() {
        app.management()
                .header("Origin", MANAGEMENT_ORIGIN)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Access-Control-Allow-Origin", is(MANAGEMENT_ORIGIN));
    }

    @Test
    public void unconfiguredOrigin() {
        app.management()
                .header("Origin", OTHER_ORIGIN)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    public void sameOrigin() {
        URILike managementUri = app.getURI(Protocol.MANAGEMENT);
        String sameOrigin = managementUri.getRestAssuredStyleUri() + ":" + managementUri.getPort();

        app.management()
                .header("Origin", sameOrigin)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Access-Control-Allow-Origin", is(sameOrigin));
    }

    @Test
    public void separateCorsPolicies() {
        app.given()
                .header("Origin", APP_ORIGIN)
                .get("/ping")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Access-Control-Allow-Origin", is(APP_ORIGIN));

        app.given()
                .header("Origin", MANAGEMENT_ORIGIN)
                .get("/ping")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .header("Access-Control-Allow-Origin", nullValue());

        app.management()
                .header("Origin", MANAGEMENT_ORIGIN)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Access-Control-Allow-Origin", is(MANAGEMENT_ORIGIN));

        app.management()
                .header("Origin", APP_ORIGIN)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    public void corsDisabledByDefault() {
        corsDisabled.management()
                .header("Origin", OTHER_ORIGIN)
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Access-Control-Allow-Origin", nullValue());
    }
}
