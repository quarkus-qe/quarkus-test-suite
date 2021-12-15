package io.quarkus.ts.reactive;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractReactiveDatabaseIT {

    @Test
    @Order(1)
    public void createAuthorWithPanache() {
        Response post = getApp().given()
                .contentType(ContentType.JSON)
                .post("/library/author/Wodehouse");
        assertEquals(HttpStatus.SC_CREATED, post.statusCode());
        String result = getApp().given()
                .when().get("/library/author/5")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("Wodehouse", result);
    }

    @Test
    @Order(2)
    public void validateWithPanache() {
        Response creation = getApp().given().contentType(ContentType.JSON).post("library/author/Subrahmanyakavi");
        assertEquals(HttpStatus.SC_BAD_REQUEST, creation.statusCode());
        getApp().given()
                .when().get("/library/author/6")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Order(3)
    public void createAuthorWithoutPanache() {
        Response post = getApp().given()
                .contentType(ContentType.JSON)
                .post("/hibernate/author/create/Plato");
        assertEquals(HttpStatus.SC_CREATED, post.statusCode());
        String result = getApp().given()
                .when().get("/library/author/7")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("Plato", result);
    }

    @Test
    public void getAll() {
        Response books = getApp().given()
                .get("/library/books");
        assertEquals(books.statusCode(), HttpStatus.SC_OK);
        String titles = books.body().asString();
        assertTrue(titles.contains("Attention and Effort"));
        assertTrue(titles.contains("Thinking fast and slow"));
    }

    @Test
    public void connectToUniEndpoint() {
        Response response = getApp().given()
                .when().get("/library/books/1");
        String title = response.body().asString();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Slovník", title);
    }

    @Test
    public void findById() {
        String title = getApp().given()
                .when().get("/library/books/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("Thinking fast and slow", title);
    }

    @Test
    public void findByInvalidId() {
        Response response = getApp().given().when().get("/library/books/256");
        assertEquals(HttpStatus.SC_NOT_FOUND, response.statusCode());
    }

    @Test
    public void connectToMultiEndpoint() {
        String result = getApp().given()
                .when().get("/library/books")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertTrue(result.contains("Slovník"));
        assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void searchByQuery() {
        getApp().given()
                .when().get("/library/authors")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void searchWithJoin() {
        String result = getApp().given()
                .when().get("/library/books/author/Kahneman")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertTrue(result.contains("Attention and Effort"));
        assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void searchWithLimit() {
        String result = getApp().given()
                .when().get("/hibernate/books/author/4")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertFalse(result.contains("Attention and Effort"));
        assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void namedQuery() {
        Response response = getApp().given()
                .when().get("/hibernate/books/starts_with/Thinking");
        String result = response.getBody().asString();
        assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void getAuthorById() {
        Response response = getApp().given()
                .when().get("/library/author/2");
        String result = response.then()
                .extract().body().asString();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Vern", result);
    }

    @Test
    public void createBookWithGeneratedId() {
        String author = getApp().given()
                .when().get("/library/author/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("Vern", author);
        Response creation = getApp().given().put("library/books/2/Around_the_World_in_Eighty_Days");
        assertEquals(HttpStatus.SC_CREATED, creation.statusCode());
        String result = getApp().given()
                .when().get("library/books/author/Vern")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("[Around_the_World_in_Eighty_Days]", result);
    }

    @Test
    public void deleteAuthorById() {
        getApp().given().delete("library/author/1")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        Response response = getApp().given()
                .when().get("/library/author/1");
        assertEquals(HttpStatus.SC_NOT_FOUND, response.statusCode());
    }

    @Test
    public void useDataTransferObjects() {
        Response response = getApp().given().when().get("library/dto/4");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        JsonPath jsonPath = response.getBody().jsonPath();
        assertEquals("Thinking fast and slow", jsonPath.getString("[0].title"));
        assertEquals("Attention and Effort", jsonPath.getString("[1].title"));
    }

    @Test
    public void useSession() {
        String title = getApp().given()
                .when().get("/hibernate/books/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertEquals("Thinking fast and slow", title);
    }

    @Test
    public void useTransaction() {
        Response creation = getApp().given()
                .contentType(ContentType.JSON)
                .post("hibernate/books/Dick/Ubik");
        assertEquals(HttpStatus.SC_CREATED, creation.statusCode());
        Response response = getApp().given()
                .when().get("/library/books/author/Dick");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("[Ubik]", response.getBody().asString());
    }

    @Test
    public void convertValue() {
        Response response = getApp().given().get("/library/isbn/2");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("9780374275631", response.body().asString());

        Response raw = getApp().given().get("/hibernate/isbn/2");
        assertEquals(HttpStatus.SC_OK, raw.statusCode());
        assertEquals("978-0374275631", raw.body().asString());
    }

    @Test
    public void convertZeroValue() {
        Response response = getApp().given().get("/library/isbn/3");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("0", response.body().asString());
    }

    @Test
    public void setConvertedValue() {
        Response change = getApp().given().put("/library/isbn/1/5170261586");
        assertEquals(HttpStatus.SC_OK, change.statusCode());
        Response lookup = getApp().given().get("/library/isbn/1");
        assertEquals(HttpStatus.SC_OK, lookup.statusCode());
        assertEquals("5170261586", lookup.body().asString());

        Response raw = getApp().given().get("/hibernate/isbn/1");
        assertEquals(HttpStatus.SC_OK, raw.statusCode());
        assertEquals("000-5-17-026158-6", raw.body().asString());
    }

    @Test
    public void validateWithoutPanache() {
        Response creation = getApp().given()
                .contentType(ContentType.JSON)
                .post("hibernate/books/Subrahmanyakavi/Atmabodhamu");
        assertEquals(HttpStatus.SC_BAD_REQUEST, creation.statusCode());
    }

    protected abstract RestService getApp();
}
