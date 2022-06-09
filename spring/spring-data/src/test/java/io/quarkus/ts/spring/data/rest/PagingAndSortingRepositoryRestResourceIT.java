package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.AbstractDbIT;
import io.restassured.response.Response;

@QuarkusScenario
class PagingAndSortingRepositoryRestResourceIT extends AbstractDbIT {

    @Test
    void testAllRepositoryMethods() throws InterruptedException {
        //GET - List all articles (should have all 4 articles the database has initially)
        app.given()
                .accept("application/json")
                .when().get("/articles")
                .then()
                .statusCode(200)
                .body(
                        containsString("Aeneid"),
                        containsString("Beach House"),
                        containsString("Cadillac Desert"),
                        containsString("Dagon and Other Macabre Tales"));

        //POST - Create a new Article
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Early Asimov\", \"author\": \"Isaac Asimov\"}")
                .when().post("/articles")
                .then()
                .statusCode(201)
                .body(containsString("Early Asimov"))
                .body("id", notNullValue())
                .extract().body().jsonPath().getString("id");

        //PUT - Update a new Article
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Early Asimov 2nd Edition\", \"author\": \"Isaac Asimov\"}")
                .when().put("/articles/5")
                .then()
                .statusCode(204);

        //GET{id} - Find new article by id
        app.given()
                .when().get("/articles/id/5")
                .then()
                .statusCode(200)
                .body(
                        containsString("Early Asimov 2nd Edition"));

        //DELETE - Try to delete a article via HTTP (method not allowed)
        app.given()
                .when().delete("/articles/5")
                .then()
                .statusCode(405);

        //Test repository pagination
        app.given()
                .accept("application/json")
                .queryParam("size", "2")
                .queryParam("page", "0")
                .when().get("/articles")
                .then()
                .statusCode(200)
                .body(
                        containsString("Aeneid"),
                        containsString("Beach House"),
                        not(containsString("Cadillac Desert")),
                        not(containsString("Dagon and Other Macabre Tales")),
                        not(containsString("Early Asimov 2nd Edition")));

        //Test repository sorting
        List<String> articleNamesSortedDesc = new ArrayList<>(Arrays.asList(
                "Early Asimov 2nd Edition",
                "Dagon and Other Macabre Tales",
                "Cadillac Desert",
                "Beach House",
                "Aeneid"));
        Response response = app.given()
                .accept("application/json")
                .queryParam("sort", "-name")
                .when().get("/articles")
                .then()
                .statusCode(200).extract().response();
        List<String> articleNamesRepositorySortedDesc = response.jsonPath().getList("name");

        assertEquals(articleNamesSortedDesc, articleNamesRepositorySortedDesc);

    }

    @Test
    void testRepositoryValidator() throws InterruptedException {
        //Try to add a article with invalid constraints
        app.given()
                .contentType("application/json")
                .body("{\"name\": \"Q\", \"author\": \"Li\"}")
                .when().post("/articles")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("length must be between 2 and 50"));
    }
}
