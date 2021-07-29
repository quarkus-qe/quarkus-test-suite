package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class LibraryRepositoryIT extends AbstractDbIT {
    @Test
    void testAllRepositoryMethods() throws InterruptedException {

        //GET - List all libraries
        app.given()
                .accept("application/json")
                .when().get("/library")
                .then()
                .statusCode(200)
                .body(
                        containsString("Library1"));

        //POST - Create a new Library
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Library2\"}")
                .when().post("/library")
                .then()
                .statusCode(201)
                .body(containsString("Library2"))
                .body("id", notNullValue())
                .extract().body().jsonPath().getString("id");

        //GET{id} - Find new library by id
        app.given()
                .when().get("/library/id/2")
                .then()
                .statusCode(200)
                .body(
                        containsString("Library2"));

        //PUT - Update library entry
        app.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Library Two\"}")
                .when().put("/library/2")
                .then()
                .statusCode(204);

        //GET{id} - Verify update
        app.given()
                .when().get("/library/id/2")
                .then()
                .statusCode(200)
                .body(
                        containsString("Library Two"));

        //DELETE - Delete a library
        app.given()
                .when().delete("/library/2")
                .then()
                .statusCode(204);
    }

    @Test
    void testRepositoryValidator() throws InterruptedException {
        //Try to add a library with invalid constraints
        app.given()
                .contentType("application/json")
                .body("{\"name\": \"\"}")
                .when().post("/library")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("Name may not be blank"));
    }
}
