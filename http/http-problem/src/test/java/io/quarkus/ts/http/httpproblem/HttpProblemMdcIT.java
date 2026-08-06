package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.HttpProblemResource;
import io.quarkus.ts.http.httpproblem.sources.MdcRequestFilter;

@Tag("QUARKUS-7309")
@QuarkusScenario
public class HttpProblemMdcIT {

    @QuarkusApplication(classes = { HttpProblemResource.class,
            MdcRequestFilter.class }, dependencies = @Dependency(artifactId = "quarkus-rest-jackson"), properties = "mdc.properties")
    static final RestService app = new RestService();

    @Test
    void testMdcPropertyIncludedInResponse() {
        app.given()
                .get("/problem/standard")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("requestId", equalTo("test-request-id-12345"));
    }
}
