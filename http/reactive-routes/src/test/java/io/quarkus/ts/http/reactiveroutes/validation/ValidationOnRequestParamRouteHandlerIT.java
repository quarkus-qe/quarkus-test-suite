package io.quarkus.ts.http.reactiveroutes.validation;

import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorDetails;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorField;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorStatus;
import static io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationAssertions.assertValidationErrorTitle;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.ts.http.reactiveroutes.validation.utils.ValidationErrorResponse;

@QuarkusScenario
@DisabledOnSemeruJdk(reason = "https://github.com/eclipse-openj9/openj9/issues/22812")
public class ValidationOnRequestParamRouteHandlerIT {

    @Test
    public void shouldGetValidationErrorWhenSingleParamIsWrong() {
        ValidationErrorResponse response = given()
                .when()
                .get("/validate/request-single-param/MA")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestSingleParam.param",
                "Param must have 3 characters");
    }

    @Test
    public void shouldBeValidWhenSingleParamIsValid() {
        given().when()
                .get("/validate/request-single-param/MAD")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldGetValidationErrorsWhenAllParamsAreWrong() {
        ValidationErrorResponse response = given()
                .get("/validate/request-multiple-param/MA/second/F12")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);
        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestMultipleParam.firstParam",
                "First param must have 3 characters");
        assertValidationErrorField(response, "validateRequestMultipleParam.secondParam",
                "Second param must match pattern");
    }

    @Test
    public void shouldBeValidWhenMultipleParamsAreValid() {
        given().when()
                .get("/validate/request-multiple-param/MAD/second/FR123")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldGetValidationErrorWhenSingleParamIsLowercase() {
        ValidationErrorResponse response = given()
                .when()
                .get("/validate/request-single-param-custom-validation/lower")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestSingleParamUsingCustomValidation.param",
                "Value must be uppercase");
    }

    @Test
    public void shouldBeValidWhenSingleParamIsUppercase() {
        given().when()
                .get("/validate/request-single-param-custom-validation/UPPER")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
