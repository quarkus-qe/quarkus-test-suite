package io.quarkus.ts.http.reactiveroutes.validation;

import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorDetails;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorField;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorStatus;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorTitle;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationErrorResponse;

@QuarkusScenario
public class ValidationOnResponseRouteHandlerIT {

    @Test
    public void shouldBeValidWhenUsingUni() {
        given().when()
                .get("/validate/response-uni-valid")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldGetValidationErrorWhenUniResponseIdIsWrong() {
        ValidationErrorResponse response = given()
                .when()
                .get("/validate/response-uni-invalid-id")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertValidationErrorField(response, "id", "id can't be null");
    }

    @Disabled("Not validating Java types. Reported in: https://github.com/quarkusio/quarkus/issues/15168")
    @Test
    public void shouldGetValidationErrorWhenUniResponseStringIsWrong() {
        ValidationErrorResponse response = given()
                .when()
                .get("/validate/response-uni-invalid-string")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
