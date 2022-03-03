package io.quarkus.qe.qute;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class LocalizedMessagesTest {

    @Tag("QUARKUS-1547")
    @ParameterizedTest
    @CsvSource({ "en, Hello. Hello Nikos", "el, Γειά. Γειά σου Nikos" })
    void useLocalizedMessageInstance(String lang, String output) {
        given()
                .when().get("/hello/" + lang)
                .then()
                .statusCode(200)
                .body(is(output));
    }

    @Test
    void useMessageBundlesAndLocalizedAnnotation() {
        given()
                .when().get("/hello/names")
                .then()
                .statusCode(200)
                .body(is("Hello Rostislav! == Hello Rostislav! | Ahoj Rostislav! == Ahoj Rostislav!"));
    }

    @Test
    void useLocalizedTemplate() {
        given()
                .when().get("/hello/page")
                .then()
                .statusCode(200)
                .body(containsString("Ahoj Rostislav!"));
    }
}
