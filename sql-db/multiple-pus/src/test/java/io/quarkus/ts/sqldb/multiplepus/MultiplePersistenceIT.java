package io.quarkus.ts.sqldb.multiplepus;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.sqldb.multiplepus.model.fruit.Fruit;
import io.quarkus.ts.sqldb.multiplepus.model.vegetable.Vegetable;
import io.restassured.http.ContentType;

@QuarkusScenario
public class MultiplePersistenceIT {

    static final int MARIADB_PORT = 3306;
    static final int POSTGRESQL_PORT = 5432;

    static final int EXPECTED_FRUITS_SIZE = 7;
    static final int EXPECTED_VEGETABLES_SIZE = 7;
    static final long INVALID_ID = 999L;

    @Container(image = "${mariadb.102.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService mariadb = new MariaDbService();

    @Container(image = "${postgresql.10.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("MARIA_DB_USERNAME", mariadb.getUser())
            .withProperty("MARIA_DB_PASSWORD", mariadb.getPassword())
            .withProperty("MARIA_DB_JDBC_URL", mariadb::getJdbcUrl)
            .withProperty("POSTGRESQL_USERNAME", postgresql.getUser())
            .withProperty("POSTGRESQL_PASSWORD", postgresql.getPassword())
            .withProperty("POSTGRESQL_JDBC_URL", postgresql::getJdbcUrl);

    private static Integer latestFruitId;
    private static Integer latestVegetableId;

    @BeforeEach
    public void cleanUp() {
        if (latestFruitId != null) {
            app.given().delete("/fruit/" + latestFruitId);
            latestFruitId = null;
        }

        if (latestVegetableId != null) {
            app.given().delete("/vegetable/" + latestVegetableId);
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
