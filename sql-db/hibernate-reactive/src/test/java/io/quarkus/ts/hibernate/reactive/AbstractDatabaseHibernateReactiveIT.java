package io.quarkus.ts.hibernate.reactive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractDatabaseHibernateReactiveIT {

    @Test
    @Order(2)
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
    @Order(3)
    public void validateWithPanache() {
        Response creation = getApp().given().contentType(ContentType.JSON).post("library/author/Subrahmanyakavi");
        assertEquals(HttpStatus.SC_BAD_REQUEST, creation.statusCode());
        getApp().given()
                .when().get("/library/author/6")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Order(4)
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
    @Order(1)
    public void searchByQuery() {
        JsonPath body = getApp().given()
                .when().get("/library/authors")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath();

        assertEquals(4, body.getList(".").size());

        assertEquals(1, body.getInt("[0].id"));
        assertEquals("Homer", body.getString("[0].name"));
        assertTrue(body.getList("[0].books").isEmpty());

        assertEquals(2, body.getInt("[1].id"));
        assertEquals("Vern", body.getString("[1].name"));
        assertTrue(body.getList("[1].books").isEmpty());

        assertEquals(3, body.getInt("[2].id"));
        assertEquals("Dlugi", body.getString("[2].name"));
        assertEquals(1, body.getList("[2].books").size());
        assertEquals("Slovník", body.getString("[2].books[0].title"));
        assertEquals(0, body.getInt("[2].books[0].isbn"));

        assertEquals(4, body.getInt("[3].id"));
        assertEquals("Kahneman", body.getString("[3].name"));
        assertEquals(2, body.getList("[3].books").size());
        assertEquals("Thinking fast and slow", body.getString("[3].books[0].title"));
        assertEquals(9780374275631L, body.getLong("[3].books[0].isbn"));
        assertEquals("Attention and Effort", body.getString("[3].books[1].title"));
        assertEquals(0, body.getInt("[3].books[1].isbn"));
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
        getApp().given()
                .when().get("library/books/author/Vern")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasItem("Around_the_World_in_Eighty_Days"));
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
        getApp().given()
                .when().get("/library/books/author/Dick")
                .then().statusCode(HttpStatus.SC_OK)
                .body("$", hasItem("Ubik"));
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

    @Tag("QUARKUS-1586")
    @Test
    public void ensureSessionIsPropagatedOnReactiveTransactions() {
        getApp().given()
                .contentType(ContentType.JSON)
                .post("hibernate/books/pablo/suntzu")
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/31117")
    public void newLineInQuery() {
        Response author = getApp().given()
                .get("/library/by-author/Dlugi");
        System.out.println(author.body().asString());
        assertEquals(HttpStatus.SC_OK, author.statusCode());
        assertThat(author.body().asString(), containsString("Slovník"));
    }

    protected abstract RestService getApp();
}
