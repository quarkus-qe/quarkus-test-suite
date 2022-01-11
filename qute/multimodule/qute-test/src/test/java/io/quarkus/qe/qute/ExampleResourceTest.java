package io.quarkus.qe.qute;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ExampleResourceTest {

    @Tag("QUARKUS-1547")
    @ParameterizedTest
    @CsvSource({ "en, Hello. Hello Nikos", "el, Γειά. Γειά σου Nikos" })
    void testHelloEndpoint(String lang, String output) {
        given()
                .when().get("/hello/" + lang)
                .then()
                .statusCode(200)
                .body(is(output));
    }

}
