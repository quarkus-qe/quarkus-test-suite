package io.quarkus.ts.sqldb.sqlapp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSqlDatabaseIT {

    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int NINE = 9;
    private static final int TEN = 10;
    private static final int ELEVEN = 11;

    private static final int VALID_ID = 8;
    private static final int INVALID_ID = 999;

    @Test
    @Order(ONE)
    public void getAll() {
        getApp().given()
                .get("/book")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(SEVEN));
    }

    @Test
    @Order(TWO)
    public void get() {
        getApp().given()
                .get("/book/7")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Perdido Street Station"))
                .body("author", equalTo("China Miéville"));
    }

    @Test
    @Order(THREE)
    public void create() {
        Book book = new Book();
        book.title = "Neuromancer";
        book.author = "William Gibson";

        getApp().given()
                .contentType(ContentType.JSON)
                .body(book)
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", equalTo(VALID_ID))
                .body("title", equalTo("Neuromancer"))
                .body("author", equalTo("William Gibson"));

        getApp().given()
                .get("/book/8")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Neuromancer"))
                .body("author", equalTo("William Gibson"));
    }

    @Test
    @Order(FOUR)
    public void createInvalidPayload() {
        getApp().given()
                .contentType(ContentType.TEXT)
                .body("")
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(FIVE)
    public void createBadPayload() {
        Book book = new Book();
        book.id = Long.valueOf(INVALID_ID);
        book.title = "foo";
        book.author = "bar";

        getApp().given()
                .contentType(ContentType.JSON)
                .body(book)
                .post("/book")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error", equalTo("unexpected ID in request"));
    }

    @Test
    @Order(SIX)
    public void update() {
        Book book = new Book();
        book.id = Long.valueOf(VALID_ID);
        book.title = "Schismatrix";
        book.author = "Bruce Sterling";

        getApp().given()
                .contentType(ContentType.JSON)
                .body(book)
                .put("/book/8")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(VALID_ID))
                .body("title", equalTo("Schismatrix"))
                .body("author", equalTo("Bruce Sterling"));

        getApp().given()
                .get("/book/" + VALID_ID)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Schismatrix"))
                .body("author", equalTo("Bruce Sterling"));
    }

    @Test
    @Order(SEVEN)
    public void updateWithUnknownId() {
        Book book = new Book();
        book.id = Long.valueOf(INVALID_ID);
        book.title = "foo";
        book.author = "bar";

        getApp().given()
                .contentType(ContentType.JSON)
                .body(book)
                .put("/book/999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '999' not found"));
    }

    @Test
    @Order(EIGHT)
    public void updateInvalidPayload() {
        getApp().given()
                .contentType(ContentType.TEXT)
                .body("")
                .put("/book/8")
                .then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(NINE)
    public void updateBadPayload() {
        Book book = new Book();

        getApp().given()
                .contentType(ContentType.JSON)
                .body(book)
                .put("/book/8")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", containsInAnyOrder("book title must be set", "book author must be set"));
    }

    @Test
    @Order(TEN)
    public void delete() {
        getApp().given()
                .delete("/book/8")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getApp().given()
                .get("/book/8")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '8' not found"));
    }

    @Test
    @Order(ELEVEN)
    public void deleteWithUnknownId() {
        getApp().given()
                .delete("/book/999")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '999' not found"));
    }

    protected abstract RestService getApp();
}
