package io.quarkus.ts.external.applications;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.restassured.http.ContentType;

@DisabledOnNative
@DisabledOnQuarkusVersion(version = "9\\..*", reason = "999-SNAPSHOT is not available in the Maven repositories in OpenShift")
@OpenShiftScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private static final String POSTGRESQL_USER = "superman";
    private static final String POSTGRESQL_PASSWORD = "superman";
    private static final String POSTGRESQL_DATABASE = "heroes-database";
    private static final int POSTGRESQL_PORT = 5432;

    @Container(image = "registry.redhat.io/rhscl/postgresql-12-rhel7", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static DefaultService database = new DefaultService()
            .withProperty("POSTGRESQL_USER", POSTGRESQL_USER)
            .withProperty("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD)
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-workshops.git", contextDir = "quarkus-workshop-super-heroes/super-heroes/rest-hero", mavenArgs = "-Dquarkus.package.type=uber-jar -DskipTests")
    static final RestService app = new RestService()
            .withProperty("quarkus.http.port", "8080")
            .withProperty("quarkus.datasource.username", POSTGRESQL_USER)
            .withProperty("quarkus.datasource.password", POSTGRESQL_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:postgresql") + ":" + database.getPort() + "/"
                            + POSTGRESQL_DATABASE);

    @Test
    public void testHello() {
        app.given()
                .get("/api/heroes/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("hello"));
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
    public void testMetrics() {
        app.given()
                .accept(ContentType.JSON)
                .when().get("/q/metrics/application")
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
                .body("level", is(DEFAULT_LEVEL * 3))
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
                .body("level", is(UPDATED_LEVEL))
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

    @Test
    @Order(4)
    public void testCalledOperationMetrics() {
        app.given()
                .accept(ContentType.JSON)
                .when().get("/q/metrics/application")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("'io.quarkus.workshop.superheroes.hero.HeroResource.countCreateHero'", is(1))
                .body("'io.quarkus.workshop.superheroes.hero.HeroResource.countUpdateHero'", is(1))
                .body("'io.quarkus.workshop.superheroes.hero.HeroResource.countDeleteHero'", is(1));
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
