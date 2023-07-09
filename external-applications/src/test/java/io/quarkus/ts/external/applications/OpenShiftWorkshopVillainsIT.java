package io.quarkus.ts.external.applications;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.hamcrest.Matchers.empty;

import org.apache.http.HttpStatus;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.restassured.http.ContentType;

@Disabled("Disabled until https://github.com/quarkus-qe/quarkus-test-framework/issues/432 is fixed")
@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftWorkshopVillainsIT {
    private static String villainId;

    private static final String DEFAULT_NAME = "Test Villain";
    private static final String UPDATED_NAME = "Updated Test Villain";
    private static final String DEFAULT_OTHER_NAME = "Other Test Villain Name";
    private static final String UPDATED_OTHER_NAME = "Updated Other Test Villain Name";
    private static final String DEFAULT_PICTURE = "harold.png";
    private static final String UPDATED_PICTURE = "hackerman.png";
    private static final String DEFAULT_POWERS = "Partakes in this test";
    private static final String UPDATED_POWERS = "Partakes in update test";
    private static final int DEFAULT_LEVEL = 42;
    private static final int UPDATED_LEVEL = 43;

    private static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-workshops.git", contextDir = "quarkus-workshop-super-heroes/super-heroes/rest-villains", mavenArgs = "-Dquarkus.package.type=uber-jar -DskipTests -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION}")
    static final RestService app = new RestService()
            .withProperty("quarkus.http.port", "8080")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void testHello() {
        app.given()
                .get("/api/villains/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello Villain Resource"));
    }

    @Test
    public void testOpenApi() {
        app.given()
                .accept(ContentType.JSON)
                .when().get("/q/openapi")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testLiveness() {
        app.given()
                .accept(ContentType.JSON)
                .when().get("/q/health/live")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testReadiness() {
        app.given()
                .accept(ContentType.JSON)
                .when().get("/q/health/ready")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(1)
    public void testCreateVillain() {
        Villain villain = new Villain();
        villain.name = DEFAULT_NAME;
        villain.otherName = DEFAULT_OTHER_NAME;
        villain.level = DEFAULT_LEVEL;
        villain.picture = DEFAULT_PICTURE;
        villain.powers = DEFAULT_POWERS;

        String location = app.given()
                .body(villain)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/api/villains")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().header("Location");
        assertTrue(location.contains("/api/villains"));

        String[] segments = location.split("/");
        villainId = segments[segments.length - 1];
        assertNotNull(villainId);

        app.given()
                .pathParam("id", villainId)
                .when().get("/api/villains/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("name", is(DEFAULT_NAME))
                .body("otherName", is(DEFAULT_OTHER_NAME))
                .body("level", not(empty()))
                .body("picture", is(DEFAULT_PICTURE))
                .body("powers", is(DEFAULT_POWERS));
    }

    @Test
    @Order(2)
    public void testUpdateVillain() {
        Villain villain = new Villain();
        villain.id = Long.valueOf(villainId);
        villain.name = UPDATED_NAME;
        villain.otherName = UPDATED_OTHER_NAME;
        villain.level = UPDATED_LEVEL;
        villain.picture = UPDATED_PICTURE;
        villain.powers = UPDATED_POWERS;

        app.given()
                .body(villain)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .put("/api/villains")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("name", Is.is(UPDATED_NAME))
                .body("otherName", Is.is(UPDATED_OTHER_NAME))
                .body("level", Is.is(UPDATED_LEVEL))
                .body("picture", Is.is(UPDATED_PICTURE))
                .body("powers", Is.is(UPDATED_POWERS));
    }

    @Test
    @Order(3)
    public void testDeleteVillain() {
        app.given()
                .pathParam("id", villainId)
                .when().delete("/api/villains/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    static class Villain {
        public Long id;
        public String name;
        public String otherName;
        public int level;
        public String picture;
        public String powers;
    }
}
