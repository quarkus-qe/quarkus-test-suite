package io.quarkus.ts.http.jaxrs;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.ResponseBodyExtractionOptions;

@Tag("QUARKUS-1554")
@QuarkusScenario
public class RESTEasyJacksonSerializationIT {

    protected final static int LONGEST_EAGER_ALLOC = 100_000;
    protected final static int FIRST_RANDOM_VARIATION = 1;
    protected final static int SECOND_RANDOM_VARIATION = 39;
    protected final static int THIRD_RANDOM_VARIATION = 3;
    protected final static int FOURTH_RANDOM_VARIATION = 9;

    @Test
    public void serializeString() {
        String result = makeQuery("/hello/serialize-string").asString();
        Assertions.assertEquals("Hello RESTEasy", result);
    }

    @Test
    public void serializeList() {
        String result = makeQuery("/hello/serialize-list").asString();
        Assertions.assertEquals("[Hello RESTEasy]", result);
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/22654")
    public void serializeBigList() {
        String basePath = "/hello/big-serialize-list?expSize=";
        // Try couple of variations just to tease out possible edge cases
        mustBeMoreThanZero(makeQuery(basePath + (LONGEST_EAGER_ALLOC - SECOND_RANDOM_VARIATION)).jsonPath().getInt("size"));
        mustBeMoreThanZero(makeQuery(basePath + (LONGEST_EAGER_ALLOC + FIRST_RANDOM_VARIATION)).jsonPath().getInt("size"));
        mustBeMoreThanZero(makeQuery(basePath + (THIRD_RANDOM_VARIATION * LONGEST_EAGER_ALLOC - FIRST_RANDOM_VARIATION))
                .jsonPath().getInt("size"));
        mustBeMoreThanZero(makeQuery(basePath + (FOURTH_RANDOM_VARIATION * LONGEST_EAGER_ALLOC)).jsonPath().getInt("size"));
    }

    private ResponseBodyExtractionOptions makeQuery(String path) {
        return given()
                .when().get(path)
                .then()
                .statusCode(200)
                .extract().body();
    }

    private void mustBeMoreThanZero(int input) {
        String errorMsg = "text serialization size must be greater than 0";
        Assertions.assertTrue(input > 0, errorMsg);
    }
}
