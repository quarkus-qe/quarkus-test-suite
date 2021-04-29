package io.quarkus.ts.openshift.sqldb.multiplepus;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.openshift.sqldb.multiplepus.model.fruit.Fruit;
import io.quarkus.ts.openshift.sqldb.multiplepus.model.vegetable.Vegetable;
import io.restassured.http.ContentType;

@QuarkusScenario
public class MultiplePersistenceIT {

    static final String MARIADB_USER = "user";
    static final String MARIADB_PASSWORD = "user";
    static final String MARIADB_DATABASE = "mydb";
    static final int MARIADB_PORT = 3306;
    static final String POSTGRESQL_USER = "user";
    static final String POSTGRESQL_PASSWORD = "user";
    static final String POSTGRESQL_DATABASE = "mydb";
    static final int POSTGRESQL_PORT = 5432;

    static final int EXPECTED_FRUITS_SIZE = 7;
    static final int EXPECTED_VEGETABLES_SIZE = 7;
    static final long INVALID_ID = 999L;

    @Container(image = "registry.access.redhat.com/rhscl/mariadb-102-rhel7", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static DefaultService mariadb = new DefaultService()
            .withProperty("MYSQL_USER", MARIADB_USER)
            .withProperty("MYSQL_PASSWORD", MARIADB_PASSWORD)
            .withProperty("MYSQL_DATABASE", MARIADB_DATABASE);

    @Container(image = "registry.access.redhat.com/rhscl/postgresql-10-rhel7", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static DefaultService postgresql = new DefaultService()
            .withProperty("POSTGRESQL_USER", POSTGRESQL_USER)
            .withProperty("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD)
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("MARIA_DB_USERNAME", MARIADB_USER)
            .withProperty("MARIA_DB_PASSWORD", MARIADB_PASSWORD)
            .withProperty("MARIA_DB_DATABASE", MARIADB_DATABASE)
            .withProperty("MARIA_DB_JDBC_URL",
                    () -> mariadb.getHost().replace("http", "jdbc:mariadb") + ":" + mariadb.getPort())
            .withProperty("POSTGRESQL_USERNAME", POSTGRESQL_USER)
            .withProperty("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD)
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE)
            .withProperty("POSTGRESQL_JDBC_URL",
                    () -> postgresql.getHost().replace("http", "jdbc:postgresql") + ":" + postgresql.getPort());

    private static Integer latestFruitId;
    private static Integer latestVegetableId;

    @BeforeEach
    public void cleanUp() {
        if (latestFruitId != null) {
            app.given()
                    .delete("/fruit/" + latestFruitId)
                    .then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
            latestFruitId = null;
        }

        if (latestVegetableId != null) {
            app.given()
                    .delete("/vegetable/" + latestVegetableId)
                    .then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
            latestVegetableId = null;
        }
    }

    @Test
    public void getAllFruits() {
        app.given()
                .get("/fruit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(EXPECTED_FRUITS_SIZE));
    }

    @Test
    public void getFruitById() {
        app.given()
                .get("/fruit/7")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Cranberry"));
    }

    @Test
    public void createFruit() {
        Fruit fruit = new Fruit();
        fruit.name = "Canteloupe";

        latestFruitId = app.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Canteloupe"))
                .extract().path("id");

        app.given()
                .get("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Canteloupe"));
    }

    @Test
    public void createInvalidPayloadFruit() {
        app.given()
                .contentType(ContentType.TEXT)
                .body("")
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    public void createInvalidIdFruit() {
        Fruit fruit = new Fruit();
        fruit.id = INVALID_ID;
        fruit.name = "foo";

        app.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error", equalTo("unexpected ID in request"));
    }

    @Test
    public void updateFruit() {
        Fruit fruit = new Fruit();
        fruit.name = "Canteloupe";

        latestFruitId = app.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Canteloupe"))
                .extract().path("id");

        Fruit updatedFruit = new Fruit();
        updatedFruit.id = (long) latestFruitId;
        updatedFruit.name = "Dragonfruit";

        app.given()
                .contentType(ContentType.JSON)
                .body(updatedFruit)
                .put("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(latestFruitId))
                .body("name", equalTo("Dragonfruit"));

        app.given()
                .get("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Dragonfruit"));
    }

    @Test
    public void updateFruitWithUnknownId() {
        Fruit fruit = new Fruit();
        fruit.id = INVALID_ID;
        fruit.name = "foo";

        app.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .put("/fruit/999")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("code", equalTo(NOT_FOUND.getStatusCode()))
                .body("error", equalTo("fruit '999' not found"));
    }

    @Test
    public void updateWithNullFruit() {
        app.given()
                .contentType(ContentType.TEXT)
                .body("")
                .put("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    public void updateWithInvalidFruit() {
        Fruit fruit = new Fruit();

        app.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .put("/fruit/1")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", contains("Fruit name must be set!"));
    }

    @Test
    public void deleteFruit() {
        Fruit fruit = new Fruit();
        fruit.name = "Canteloupe";

        latestFruitId = app.given()
                .when()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Canteloupe"))
                .extract().path("id");

        app.given()
                .delete("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .get("/fruit/" + latestFruitId)
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("code", equalTo(NOT_FOUND.getStatusCode()))
                .body("error", equalTo("fruit '" + latestFruitId + "' not found"));
    }

    @Test
    public void deleteFruitWithUnknownId() {
        app.given()
                .delete("/fruit/999")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("code", equalTo(NOT_FOUND.getStatusCode()))
                .body("error", equalTo("fruit '999' not found"));
    }

    @Test
    public void getAllVegetables() {
        app.given()
                .get("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(EXPECTED_VEGETABLES_SIZE));
    }

    @Test
    public void getVegetableById() {
        app.given()
                .get("/vegetable/7")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Garlic"));
    }

    @Test
    public void createVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.name = "Eggplant";

        latestVegetableId = app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Eggplant"))
                .extract().path("id");

        app.given()
                .get("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Eggplant"));
    }

    @Test
    public void createNullVegetable() {
        app.given()
                .contentType(ContentType.TEXT)
                .body("")
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    public void createInvalidIdVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.id = INVALID_ID;
        vegetable.name = "foo";

        app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error", equalTo("unexpected ID in request"));
    }

    @Test
    public void updateVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.name = "Eggplant";

        latestVegetableId = app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Eggplant"))
                .extract().path("id");

        Vegetable updatedVegetable = new Vegetable();
        updatedVegetable.id = (long) latestVegetableId;
        updatedVegetable.name = "Okra";

        app.given()
                .contentType(ContentType.JSON)
                .body(updatedVegetable)
                .put("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(latestVegetableId))
                .body("name", equalTo("Okra"));

        app.given()
                .get("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Okra"));
    }

    @Test
    public void updateVegetableWithUnknownId() {
        Vegetable vegetable = new Vegetable();
        vegetable.id = INVALID_ID;
        vegetable.name = "foo";

        app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .put("/vegetable/999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("vegetable '999' not found"));
    }

    @Test
    public void updateWithNullVegetable() {
        app.given()
                .contentType(ContentType.TEXT)
                .body("")
                .put("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    public void updateWithInvalidVegetable() {
        Vegetable vegetable = new Vegetable();

        app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .put("/vegetable/1")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", contains("Vegetable name must be set!"));
    }

    @Test
    public void deleteVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.name = "Eggplant";

        latestVegetableId = app.given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Eggplant"))
                .extract().path("id");

        app.given()
                .delete("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .get("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("vegetable '" + latestVegetableId + "' not found"));
    }

    @Test
    public void deleteVegetableWithUnknownId() {
        app.given()
                .delete("/vegetable/999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("vegetable '999' not found"));
    }
}
