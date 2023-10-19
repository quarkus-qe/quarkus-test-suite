package io.quarkus.ts.external.applications;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.hamcrest.Matchers.empty;

import org.apache.http.HttpStatus;
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

@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftWorkshopHeroesIT {
    private static String heroId;

    private static final String DEFAULT_NAME = "Test Hero";
    private static final String UPDATED_NAME = "Updated Test Hero";
    private static final String DEFAULT_OTHER_NAME = "Other Test Hero Name";
    private static final String UPDATED_OTHER_NAME = "Updated Other Test Hero Name";
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

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-workshops.git", branch = "a5e3f48a6eabf4145ec01d4644dd3d5e24371b40", contextDir = "quarkus-workshop-super-heroes/super-heroes/rest-heroes", mavenArgs = "-Dquarkus.package.type=uber-jar -DskipTests -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION}")
    static final RestService app = new RestService()
            .withProperty("quarkus.http.port", "8080")
            .withProperty("quarkus.datasource.reactive.url", () -> database.getReactiveUrl())
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void testHello() {
        app.given()
                .get("/api/heroes/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello Hero Resource"));
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
    public void testCreateHero() {
        Hero hero = new Hero();
        hero.name = DEFAULT_NAME;
        hero.otherName = DEFAULT_OTHER_NAME;
        hero.level = DEFAULT_LEVEL;
        hero.picture = DEFAULT_PICTURE;
        hero.powers = DEFAULT_POWERS;

        String location = app.given()
                .body(hero)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/api/heroes")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().header("Location");
        assertTrue(location.contains("/api/heroes"));

        String[] segments = location.split("/");
        heroId = segments[segments.length - 1];
        assertNotNull(heroId);

        app.given()
                .pathParam("id", heroId)
                .when().get("/api/heroes/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("name", is(DEFAULT_NAME))
                .body("otherName", is(DEFAULT_OTHER_NAME))
                .body("level", is(DEFAULT_LEVEL))
                .body("picture", is(DEFAULT_PICTURE))
                .body("powers", is(DEFAULT_POWERS));
    }

    @Test
    @Order(2)
    public void testUpdateHero() {
        Hero hero = new Hero();
        hero.id = Long.valueOf(heroId);
        hero.name = UPDATED_NAME;
        hero.otherName = UPDATED_OTHER_NAME;
        hero.level = UPDATED_LEVEL;
        hero.picture = UPDATED_PICTURE;
        hero.powers = UPDATED_POWERS;

        app.given()
                .body(hero)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .put("/api/heroes")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("name", is(UPDATED_NAME))
                .body("otherName", is(UPDATED_OTHER_NAME))
                .body("level", not(empty()))
                .body("picture", is(UPDATED_PICTURE))
                .body("powers", is(UPDATED_POWERS));
    }

    @Test
    @Order(3)
    public void testDeleteHero() {
        app.given()
                .pathParam("id", heroId)
                .when().delete("/api/heroes/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    static class Hero {
        public Long id;
        public String name;
        public String otherName;
        public int level;
        public String picture;
        public String powers;
    }
}
