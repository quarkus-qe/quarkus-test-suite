package io.quarkus.ts.sqldb.compatibility;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.ts.sqldb.sqlapp.Book;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSqlDatabaseIT {

    private static final int EXPECTED_SIZE = 7;

    private static final int VALID_ID = 8;
    private static final int INVALID_ID = 999;

    @Test
    @Order(1)
    public void getAll() {
        given()
                .get("/book")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(EXPECTED_SIZE));
    }

    @Test
    @Order(2)
    public void get() {
        given()
                .get("/book/7")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Perdido Street Station"))
                .body("author", equalTo("China Miéville"));
    }

    @Test
    @Order(3)
    public void getWithUnknownId() {
        given()
                .get("/book/" + INVALID_ID)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo(String.format("book '%d' not found", INVALID_ID)));
    }

    @Test
    @Order(4)
    public void create() {
        Book book = new Book();
        book.title = "Neuromancer";
        book.author = "William Gibson";

        given()
                .contentType(ContentType.JSON)
                .body(book)
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", equalTo(VALID_ID))
                .body("title", equalTo(book.title))
                .body("author", equalTo(book.author));

        given()
                .get("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo(book.title))
                .body("author", equalTo(book.author));
    }

    @Test
    @Order(5)
    public void createInvalidPayload() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(6)
    public void createBadPayload() {
        Book book = new Book();
        book.id = (long) INVALID_ID;
        book.title = "foo";
        book.author = "bar";

        given()
                .contentType(ContentType.JSON)
                .body(book)
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error", equalTo("unexpected ID in request"));
    }

    @Test
    @Order(7)
    public void update() {
        Book book = new Book();
        book.id = (long) VALID_ID;
        book.title = "Schismatrix";
        book.author = "Bruce Sterling";

        given()
                .contentType(ContentType.JSON)
                .body(book)
                .put("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(VALID_ID))
                .body("title", equalTo(book.title))
                .body("author", equalTo(book.author));

        given()
                .get("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo(book.title))
                .body("author", equalTo(book.author));
    }

    @Test
    @Order(8)
    public void updateInvalidPayload() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .put("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(9)
    public void updateBadPayload() {
        Book book = new Book();

        given()
                .contentType(ContentType.JSON)
                .body(book)
                .put("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", containsInAnyOrder("book title must be set", "book author must be set"));
    }

    @Test
    @Order(10)
    public void delete() {
        given()
                .delete("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        given()
                .get("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo(String.format("book '%d' not found", VALID_ID)));
    }
}
