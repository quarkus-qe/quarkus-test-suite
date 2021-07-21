package io.quarkus.ts.validation;

import static io.quarkus.ts.validation.utils.ValidationAssertions.assertValidationErrorDetails;
import static io.quarkus.ts.validation.utils.ValidationAssertions.assertValidationErrorField;
import static io.quarkus.ts.validation.utils.ValidationAssertions.assertValidationErrorStatus;
import static io.quarkus.ts.validation.utils.ValidationAssertions.assertValidationErrorTitle;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.validation.utils.ValidationErrorResponse;
import io.vertx.core.json.Json;

@QuarkusScenario
public class ValidationOnRequestBodyRouteHandlerIT {

    @Test
    public void shouldGetValidationErrorWhenRequestFirstCodeIsWrong() {
        Request request = new Request();
        request.setFirstCode("MA");

        ValidationErrorResponse response = given()
                .when()
                .body(Json.encode(request))
                .post("/validate/request-body")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestBody.param.firstCode", "First code must have 3 characters");
    }

    @Test
    public void shouldGetValidationErrorsWhenFirstAndSecondCodesAreWrong() {

        Request request = new Request();
        request.setFirstCode("MA");
        request.setSecondCode("F12");

        ValidationErrorResponse response = given()
                .body(Json.encode(request))
                .post("/validate/request-body")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);
        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestBody.param.firstCode", "First code must have 3 characters");
        assertValidationErrorField(response, "validateRequestBody.param.secondCode", "Second second must match pattern");
    }

    @Test
    public void shouldGetValidationErrorWhenSingleParamIsLowercase() {
        Request request = new Request();
        request.setFirstCode("MAD");
        request.setSecondCode("FR123");
        request.setCustom("lower");

        ValidationErrorResponse response = given()
                .when()
                .body(Json.encode(request))
                .post("/validate/request-body")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().as(ValidationErrorResponse.class);

        assertValidationErrorTitle(response);
        assertValidationErrorDetails(response);
        assertValidationErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        assertValidationErrorField(response, "validateRequestBody.param.custom", "Value must be uppercase");
    }

    @Test
    public void shouldBeValidWhenTheRequestIsOk() {
        Request request = new Request();
        request.setFirstCode("MAD");
        request.setSecondCode("FR123");
        request.setCustom("UPPER");

        given().when()
                .body(Json.encode(request))
                .post("/validate/request-body")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
