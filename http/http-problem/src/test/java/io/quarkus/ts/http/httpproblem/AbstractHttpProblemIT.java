package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;

@Tag("QUARKUS-7309")
public abstract class AbstractHttpProblemIT {

    protected abstract RestService getApp();

    @Test
    void testStandardProblemFields() {
        getApp().given()
                .get("/problem/standard")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("type", equalTo("http://localhost/problem/conflict"))
                .body("title", equalTo("Conflict"))
                .body("status", equalTo(409))
                .body("detail", equalTo("Resource already exists"))
                .body("instance", equalTo("/problem/standard"));
    }

    @Test
    void testInstanceFieldPreservesPathSeparators() {
        getApp().given()
                .get("/problem/instance-path/api/resources/42")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("instance", equalTo("/api/resources/42"));
    }

    @Test
    void testCustomFieldsAndHeaders() {
        getApp().given()
                .get("/problem/custom")
                .then()
                .statusCode(422)
                .contentType("application/problem+json")
                .header("X-Custom-Header", "custom-value")
                .body("status", equalTo(422))
                .body("title", equalTo("Validation Failed"))
                .body("balance", equalTo(30))
                .body("currency", equalTo("USD"));
    }

    @Test
    void testNotFoundExceptionMapper() {
        getApp().given()
                .get("/problem/not-found")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("status", equalTo(404));
    }

    @Test
    void testForbiddenExceptionMapper() {
        getApp().given()
                .get("/problem/forbidden")
                .then()
                .statusCode(403)
                .contentType("application/problem+json")
                .body("status", equalTo(403));
    }

    @Test
    void testUnauthorizedExceptionMapper() {
        getApp().given()
                .get("/problem/unauthorized")
                .then()
                .statusCode(401)
                .contentType("application/problem+json")
                .body("status", equalTo(401));
    }

    @Test
    void testWebApplicationExceptionMapper() {
        getApp().given()
                .get("/problem/web-app-exception/429")
                .then()
                .statusCode(429)
                .contentType("application/problem+json")
                .body("status", equalTo(429));
    }

    @Test
    void testConstraintViolation400() {
        getApp().given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"value\":\"x\"}")
                .post("/problem/validate")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .body("violations", notNullValue());
    }

    @Test
    void testProblemPostProcessor() {
        getApp().given()
                .get("/problem/post-processed")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("processed", equalTo(true));
    }

    @Test
    void testUnhandledExceptionNoStackTrace() {
        String body = getApp().given()
                .get("/problem/unhandled")
                .then()
                .statusCode(500)
                .contentType("application/problem+json")
                .body("status", equalTo(500))
                .extract().body().asString();
        assertFalse(body.contains("RuntimeException"), "Stack trace should not leak: " + body);
        assertFalse(body.contains(".java:"), "Stack trace should not leak: " + body);
        assertFalse(body.contains("internal error details should not leak"),
                "Exception message should not leak: " + body);
    }
}
