package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.validation.Body;
import io.restassured.http.ContentType;

@Tag("QUARKUS-7189")
@QuarkusScenario
public class ConstraintValidationIT {

    private static final String BASE_PATH = "/client/constraint-validation";
    private static final String BODY_ONE_PROPERTY = "Body1Property";
    private static final String BODY_TWO_PROPERTY = "Body2Property";
    private static final List<Body> BODY_LIST = Arrays.asList(new Body(BODY_ONE_PROPERTY), new Body(BODY_TWO_PROPERTY));

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void checkPostValidationContainingNullCheck() {
        app.given()
                .contentType(ContentType.JSON)
                .body(BODY_LIST)
                .when().post(BASE_PATH + "/not-null-list")
                .then()
                .statusCode(200)
                .body(stringContainsInOrder(BODY_ONE_PROPERTY, BODY_TWO_PROPERTY));
    }

    @Test
    public void checkPostValidationContainingNullCheckWithNullList() {
        app.given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/not-null-list")
                .then()
                .statusCode(400)
                .body(containsString("must not be null"));
    }

    @Test
    public void checkPostValidationWithArgument() {
        app.given()
                .contentType(ContentType.JSON)
                .body(BODY_LIST)
                .queryParam("query", "valid")
                .when().post(BASE_PATH + "/query-and-list")
                .then()
                .statusCode(200)
                .body(stringContainsInOrder(BODY_ONE_PROPERTY, BODY_TWO_PROPERTY));
    }

    @Test
    public void checkPostValidationWithIllegalArgument() {
        app.given()
                .contentType(ContentType.JSON)
                .body(BODY_LIST)
                .queryParam("query", "not")
                .when().post(BASE_PATH + "/query-and-list")
                .then()
                .statusCode(400)
                .body(containsString("size must be between"));
    }

}
