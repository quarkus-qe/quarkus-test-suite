package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.ts.spring.data.AbstractDbIT;
import io.restassured.response.ValidatableResponse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractRepositoryRestResourceIT extends AbstractDbIT {

    protected abstract String getUrl();

    protected abstract List<String> getOriginalItems();

    protected abstract String getCreatedItem();

    protected abstract long getCreatedItemId();

    protected abstract String getUpdatedItem();

    protected abstract String getItemNameHalJsonPath();

    protected abstract String getItemIdUrl(long id);

    protected int getExpectedDeleteResponseStatus() {
        return HttpStatus.SC_NO_CONTENT;
    }

    protected int getExpectedDeleteInvalidResponseStatus() {
        return HttpStatus.SC_NOT_FOUND;
    }

    @Order(1)
    @Test
    void getAll() {
        //GET - List all items
        ValidatableResponse response = app.given()
                .accept("application/json")
                .when().get(getUrl())
                .then()
                .statusCode(HttpStatus.SC_OK);
        getOriginalItems().stream()
                .map(CoreMatchers::containsString)
                .forEach(response::body);
    }

    @Order(2)
    @Test
    void getAllHal() {
        //GET - List all items in hal+json format
        List<String> articlesList = app.given()
                .accept("application/hal+json")
                .when().get(getUrl())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().getList(getItemNameHalJsonPath());
        assertEquals(getOriginalItems(), articlesList);
    }

    @Order(3)
    @Test
    void create() {
        //POST - Create a new item
        long articleId = app.given()
                .contentType("application/json")
                .accept("application/json")
                .body(String.format("{\"name\": \"%s\", \"author\": \"Isaac Asimov\"}", getCreatedItem()))
                .when().post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(containsString(getCreatedItem()))
                .body("id", notNullValue())
                .extract().body().jsonPath().getLong("id");
        assertEquals(getCreatedItemId(), articleId);
    }

    @Order(4)
    @Test
    void getNewItemById() {
        //GET{id} - Find the new item by id
        app.given()
                .when().get(getItemIdUrl(getCreatedItemId()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(getCreatedItem()));
    }

    @Order(5)
    @Test
    void update() {
        //PUT - Update the new item
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Early Asimov 2nd Edition\", \"author\": \"Isaac Asimov\"}")
                .body(String.format("{\"name\": \"%s\", \"author\": \"Isaac Asimov\"}", getUpdatedItem()))
                .when().put(getUrl() + "/" + getCreatedItemId())
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Order(6)
    @Test
    void getUpdatedItemById() {
        //GET{id} - Find the updated item by id
        app.given()
                .when().get(getItemIdUrl(getCreatedItemId()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(getUpdatedItem()));
    }

    @Order(7)
    @Test
    void delete() {
        //DELETE - Delete the item
        app.given()
                .when().delete(getUrl() + "/" + getCreatedItemId())
                .then()
                .statusCode(getExpectedDeleteResponseStatus());
    }

    @Test
    void getByInvalidId() {
        app.given()
                .when().get(getItemIdUrl(999))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void deleteByInvalidId() {
        app.given()
                .when().delete(getUrl() + "/999")
                .then()
                .statusCode(getExpectedDeleteInvalidResponseStatus());
    }

    @Test
    void createInvalidEntity() {
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"invalid\": \"entity\"}")
                .when().post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void updateInvalidEntity() {
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"invalid\": \"entity\"}")
                .when().put(getUrl() + "/2")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
