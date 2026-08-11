package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.HttpProblemResource;

@Tag("QUARKUS-7309")
@QuarkusScenario
public class HttpProblemJsonBIT {

    @QuarkusApplication(classes = HttpProblemResource.class, dependencies = @Dependency(artifactId = "quarkus-rest-jsonb"))
    static final RestService app = new RestService();

    @Test
    void testProblemSerializedWithJsonB() {
        app.given()
                .get("/problem/standard")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("type", equalTo("http://localhost/problem/conflict"))
                .body("title", equalTo("Conflict"))
                .body("status", equalTo(409))
                .body("detail", equalTo("Resource already exists"));
    }

    @Test
    void testCustomFieldsWithJsonB() {
        app.given()
                .get("/problem/custom")
                .then()
                .statusCode(422)
                .contentType("application/problem+json")
                .body("balance", equalTo(30))
                .body("currency", equalTo("USD"));
    }
}
