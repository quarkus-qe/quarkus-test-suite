package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public abstract class BaseResteasyIT {

    public static final String REACTIVE = "/reactive";
    public static final String CLASSIC = "/classic";
    public static final String ENDPOINT_WITH_NO_PRODUCES = "/validate-no-produces/boom";
    public static final String ENDPOINT_WITH_MULTIPLE_PRODUCES = "/validate-multiple-produces/boom";
    public static final String CLASSIC_ENDPOINT_WITH_NO_PRODUCES = CLASSIC + ENDPOINT_WITH_NO_PRODUCES;
    public static final String CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES = CLASSIC + ENDPOINT_WITH_MULTIPLE_PRODUCES;
    public static final String REACTIVE_ENDPOINT_WITH_NO_PRODUCES = REACTIVE + ENDPOINT_WITH_NO_PRODUCES;
    public static final String REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES = REACTIVE + ENDPOINT_WITH_MULTIPLE_PRODUCES;

    private static final String EXPECTED_VALIDATOR_ERROR_MESSAGE = "numeric value out of bounds";

    protected void assertBadRequestInJsonFormat(String path) {
        assertBadRequestInJsonFormat(given().get(path));
    }

    protected void assertBadRequestInJsonFormat(Response response) {
        isBadRequest(response)
                .contentType(ContentType.JSON)
                .body("parameterViolations[0].message", containsString(EXPECTED_VALIDATOR_ERROR_MESSAGE));
    }

    protected void assertBadRequestInXmlFormat(Response response) {
        isBadRequest(response)
                .contentType(ContentType.XML)
                .body("violationReport.parameterViolations.message", containsString(EXPECTED_VALIDATOR_ERROR_MESSAGE));
    }

    protected void assertBadRequestInTextFormat(String path) {
        assertBadRequestInTextFormat(given().get(path));
    }

    protected void assertBadRequestInTextFormat(Response response) {
        isBadRequest(response)
                .contentType(ContentType.TEXT)
                .body(containsString(EXPECTED_VALIDATOR_ERROR_MESSAGE));
    }

    private ValidatableResponse isBadRequest(Response response) {
        return response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
