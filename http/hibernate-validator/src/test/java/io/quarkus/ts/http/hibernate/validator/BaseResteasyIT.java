package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

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

    protected static ResponseValidator validate(String path) {
        return BaseResteasyIT.validate(given().get(path));
    }

    protected static ResponseValidator validate(Response response) {
        return new ResponseValidator(response);
    }

    static class ResponseValidator {
        final Response response;

        private ResponseValidator(Response response) {
            this.response = response;
        }

        protected ResponseValidator isBadRequest() {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode());
            return this;
        }

        protected ResponseValidator hasReactiveJsonError() {
            response.then().contentType(ContentType.JSON);
            assertEquals(EXPECTED_VALIDATOR_ERROR_MESSAGE, response.body().jsonPath().getString("violations[0].message"));
            return this;
        }

        protected ResponseValidator hasClassicJsonError() {
            response.then().contentType(ContentType.JSON);
            assertEquals(EXPECTED_VALIDATOR_ERROR_MESSAGE,
                    response.body().jsonPath().getString("parameterViolations[0].message"));
            return this;
        }

        protected ResponseValidator hasReactiveXMLError() {
            response.then().contentType(ContentType.XML);
            assertEquals(EXPECTED_VALIDATOR_ERROR_MESSAGE,
                    response.body().xmlPath().getString("violationReport.violations.message"));
            return this;
        }

        protected ResponseValidator hasClassicXMLError() {
            response.then().contentType(ContentType.XML);
            assertEquals(EXPECTED_VALIDATOR_ERROR_MESSAGE,
                    response.body().xmlPath().getString("violationReport.parameterViolations.message"));
            return this;
        }

        protected ResponseValidator hasTextError() {
            response.then().contentType(ContentType.TEXT);
            String body = response.body().asString();
            assertTrue(body.contains(EXPECTED_VALIDATOR_ERROR_MESSAGE), body);
            return this;
        }
    }
}
