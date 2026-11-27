package io.quarkus.ts.http.jakartarest.reactive.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;

@Tag("QQE-2476")
@QuarkusScenario
public class SequencedCollectionIT {

    private static final String BASE_PATH = "/collections/sequenced";

    @BeforeEach
    public void reset() {
        given()
                .when()
                .delete(BASE_PATH)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void shouldAddToBothEnds() {
        addLast("A");
        addFirst("B");
        addLast("C");

        assertOrder(BASE_PATH, "B", "A", "C");
    }

    @Test
    public void shouldReadReversedOrder() {
        addLast("1");
        addLast("2");
        addLast("3");

        assertOrder(BASE_PATH + "/reversed", "3", "2", "1");
    }

    private void assertOrder(String path, String... expected) {
        given()
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", contains(expected));
    }

    private void addFirst(String value) {
        addAt("/first", value);
    }

    private void addLast(String value) {
        addAt("/last", value);
    }

    private void addAt(String path, String value) {
        given()
                .contentType(ContentType.TEXT)
                .body(value)
                .when()
                .post(BASE_PATH + path)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }
}
