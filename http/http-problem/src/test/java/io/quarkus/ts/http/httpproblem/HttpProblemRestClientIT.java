package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.HttpProblemResource;
import io.quarkus.ts.http.httpproblem.sources.ProblemClient;
import io.quarkus.ts.http.httpproblem.sources.ProblemClientResource;

@Tag("QUARKUS-7309")
@QuarkusScenario
public class HttpProblemRestClientIT {

    @QuarkusApplication(classes = { HttpProblemResource.class, ProblemClient.class,
            ProblemClientResource.class }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(artifactId = "quarkus-rest-client-jackson")
            }, properties = "rest-client.properties")
    static final RestService app = new RestService();

    @Test
    void testRestClientDeserializesProblem() {
        app.given()
                .get("/problem/client-test")
                .then()
                .statusCode(200)
                .body("exceptionClass", equalTo("io.quarkiverse.httpproblem.HttpProblem"))
                .body("status", equalTo(404));
    }
}
