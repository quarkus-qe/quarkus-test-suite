package io.quarkus.ts.sqldb.sqlapp;

import static io.quarkus.ts.sqldb.sqlapp.resources.FruitsResource.TEXT_BLOCK_EXAMPLE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.sqldb.sqlapp.exception.ValidationExceptionMapper;
import io.quarkus.ts.sqldb.sqlapp.model.Fruit;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@DisabledOnNative(reason = "Works in native, but we only provide DEV support")
@QuarkusScenario
public class FruitsResourceIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");

    @Test
    public void listFruitTest() {
        var fruit = createFruit(new Fruit("Pear_listed", "Winter fruit"));
        listFruits().body("$.size()", greaterThan(0));

        deleteFruit(fruit.id());
    }

    @Test
    public void addFruitTest() {
        var addedName = "Pear_added";
        var fruit = createFruit(new Fruit(addedName, "Winter fruit"));
        assertEquals(addedName, fruit.name());

        deleteFruit(fruit.id());
    }

    @Test
    public void updateFruitTest() {
        var fruitName = "banana";
        var fruitNameUpdated = String.format("%s_updated", fruitName);
        var banana = createFruit(new Fruit(fruitName, "tropical fruit"));
        var bananaUpdated = updateFruit(new Fruit(banana.id(), fruitNameUpdated, banana.description()));

        assertEquals(fruitNameUpdated, bananaUpdated.name());
        deleteFruit(bananaUpdated.id());
    }

    @Test
    public void addWrongFruitTest() {
        var invalidFruit = new Fruit("tooooolooooongToBeValid", "Winter fruit");
        given()
                .contentType(ContentType.JSON)
                .body(invalidFruit)
                .when()
                .post("/fruits")
                .then()
                .statusCode(ValidationExceptionMapper.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteFruitTest() {
        var addedName = "Pear_to_delete";
        var fruit = createFruit(new Fruit(addedName, "Winter fruit"));
        deleteFruit(fruit.id());
    }

    @Test
    public void findExistingFruitTest() {
        var fruit = createFruit(new Fruit("Kiwi", "Green fruit"));
        given()
                .when().get("/fruits/" + fruit.id())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Kiwi"));
        deleteFruit(fruit.id());
    }

    @Test
    public void findMissingFruitTest() {
        given()
                .when().get("/fruits/999999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void textBlockTest() {
        given()
                .when().get("/fruits/text-block")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(TEXT_BLOCK_EXAMPLE));
    }

    @Test
    public void sqlTextBlockQueryTest() {
        var longestDescription = "Longest description!";
        var fruit = createFruit(new Fruit("Dragonfruit", longestDescription));

        given()
                .when().get("/fruits/longest-description")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("description", equalTo(longestDescription));

        deleteFruit(fruit.id());
    }

    private Fruit createFruit(Fruit fruit) {
        return given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .when()
                .post("/fruits")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().as(Fruit.class);
    }

    private ValidatableResponse listFruits() {
        return given()
                .when().get("/fruits")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private Fruit updateFruit(Fruit fruit) {
        return given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .when()
                .put("/fruits/" + fruit.id())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().as(Fruit.class);
    }

    private void deleteFruit(Long id) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/fruits/" + id)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
