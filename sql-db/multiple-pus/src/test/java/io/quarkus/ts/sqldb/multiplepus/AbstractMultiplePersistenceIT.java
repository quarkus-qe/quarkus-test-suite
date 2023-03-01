package io.quarkus.ts.sqldb.multiplepus;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.sqldb.multiplepus.model.fruit.Fruit;
import io.quarkus.ts.sqldb.multiplepus.model.vegetable.Vegetable;
import io.restassured.http.ContentType;

public abstract class AbstractMultiplePersistenceIT {

    static final int EXPECTED_FRUITS_SIZE = 7;
    static final int EXPECTED_VEGETABLES_SIZE = 7;
    static final long INVALID_ID = 999L;

    static Integer latestFruitId;
    static Integer latestVegetableId;

    @BeforeEach
    public void cleanUp() {
        if (latestFruitId != null && getApp() != null) {
            getApp().given().delete("/fruit/" + latestFruitId);
            latestFruitId = null;
        }

        if (latestVegetableId != null && getApp() != null) {
            getApp().given().delete("/vegetable/" + latestVegetableId);
            latestVegetableId = null;
        }
    }

    abstract RestService getApp();

    @Test
    public void getAllFruits() {
        getApp().given()
                .get("/fruit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(EXPECTED_FRUITS_SIZE));
    }

    @Test
    public void getFruitById() {
        getApp().given()
                .get("/fruit/301")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Cranberry"));
    }

    @Test
    public void createFruit() {
        Fruit fruit = new Fruit();
        fruit.name = "Canteloupe";

        latestFruitId = getApp().given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Canteloupe"))
                .extract().path("id");

        getApp().given()
                .get("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Canteloupe"));
    }

    @Test
    public void createInvalidPayloadFruit() {
        getApp().given()
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

        getApp().given()
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

        latestFruitId = getApp().given()
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

        getApp().given()
                .contentType(ContentType.JSON)
                .body(updatedFruit)
                .put("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(latestFruitId))
                .body("name", equalTo("Dragonfruit"));

        getApp().given()
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

        getApp().given()
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
        getApp().given()
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

        getApp().given()
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

        latestFruitId = getApp().given()
                .when()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post("/fruit")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Canteloupe"))
                .extract().path("id");

        getApp().given()
                .delete("/fruit/" + latestFruitId)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getApp().given()
                .get("/fruit/" + latestFruitId)
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("code", equalTo(NOT_FOUND.getStatusCode()))
                .body("error", equalTo("fruit '" + latestFruitId + "' not found"));
    }

    @Test
    public void deleteFruitWithUnknownId() {
        getApp().given()
                .delete("/fruit/999")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("code", equalTo(NOT_FOUND.getStatusCode()))
                .body("error", equalTo("fruit '999' not found"));
    }

    @Test
    public void getAllVegetables() {
        getApp().given()
                .get("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(EXPECTED_VEGETABLES_SIZE));
    }

    @Test
    public void getVegetableById() {
        getApp().given()
                .get("/vegetable/301")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Garlic"));
    }

    @Test
    public void createVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.name = "Eggplant";

        latestVegetableId = getApp().given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Eggplant"))
                .extract().path("id");

        getApp().given()
                .get("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Eggplant"));
    }

    @Test
    public void createNullVegetable() {
        getApp().given()
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

        getApp().given()
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

        latestVegetableId = getApp().given()
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

        getApp().given()
                .contentType(ContentType.JSON)
                .body(updatedVegetable)
                .put("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(latestVegetableId))
                .body("name", equalTo("Okra"));

        getApp().given()
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

        getApp().given()
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
        getApp().given()
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

        getApp().given()
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

        latestVegetableId = getApp().given()
                .contentType(ContentType.JSON)
                .body(vegetable)
                .post("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo("Eggplant"))
                .extract().path("id");

        getApp().given()
                .delete("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getApp().given()
                .get("/vegetable/" + latestVegetableId)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("vegetable '" + latestVegetableId + "' not found"));
    }

    @Test
    public void deleteVegetableWithUnknownId() {
        getApp().given()
                .delete("/vegetable/999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("vegetable '999' not found"));
    }
}
