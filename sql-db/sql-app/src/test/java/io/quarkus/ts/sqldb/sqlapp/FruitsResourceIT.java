package io.quarkus.ts.sqldb.sqlapp;

import static io.quarkus.ts.sqldb.sqlapp.resources.FruitsResource.TEXT_BLOCK_EXAMPLE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

// import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.sqldb.sqlapp.exception.ValidationExceptionMapper;
import io.quarkus.ts.sqldb.sqlapp.model.Fruit;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
public class FruitsResourceIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void listFruitTest() {
        listFruits().body("$.size()", greaterThan(0));
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
        var longInvalidName = "tooooolooooongToBeValid";
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + longInvalidName + "\", \"description\": \"Winter fruit\"}")
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
    public void multilinePlainTextTest() {
        given()
                .when().get("/fruits/jpe_378")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(TEXT_BLOCK_EXAMPLE));
    }

    @Test
    public void SqlMultilineTextTest() {
        given()
                .when().get("/fruits/longestDesc")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("description", equalTo("Tropical fruit"));
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
