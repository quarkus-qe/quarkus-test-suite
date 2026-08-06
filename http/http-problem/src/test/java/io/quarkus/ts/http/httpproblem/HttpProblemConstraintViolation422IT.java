package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.ValidatedBean;
import io.quarkus.ts.http.httpproblem.sources.ValidationResource;
import io.restassured.http.ContentType;

@Tag("QUARKUS-7309")
@QuarkusScenario
public class HttpProblemConstraintViolation422IT {

    @QuarkusApplication(classes = { ValidationResource.class, ValidatedBean.class }, dependencies = {
            @Dependency(artifactId = "quarkus-rest-jackson"),
            @Dependency(artifactId = "quarkus-hibernate-validator")
    }, properties = "constraint-violation-422.properties")
    static final RestService app = new RestService();

    @Test
    void testConstraintViolationReturns422() {
        app.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"value\":\"x\"}")
                .post("/problem/validate")
                .then()
                .statusCode(422)
                .contentType("application/problem+json")
                .body("status", equalTo(422))
                .body("violations", notNullValue());
    }
}
