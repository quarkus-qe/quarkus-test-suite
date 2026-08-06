package io.quarkus.ts.http.httpproblem;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.CustomProblemPostProcessor;
import io.quarkus.ts.http.httpproblem.sources.HttpProblemResource;
import io.quarkus.ts.http.httpproblem.sources.ValidatedBean;
import io.quarkus.ts.http.httpproblem.sources.ValidationResource;

@Tag("QUARKUS-7309")
@QuarkusScenario
public class HttpProblemIT extends AbstractHttpProblemIT {

    @QuarkusApplication(classes = { HttpProblemResource.class, ValidationResource.class, ValidatedBean.class,
            CustomProblemPostProcessor.class }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(artifactId = "quarkus-hibernate-validator"),
                    @Dependency(artifactId = "quarkus-smallrye-openapi")
            })
    static final RestService app = new RestService();

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    void testOpenApiContainsProblemSchema() {
        app.given()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("HttpProblem"))
                .body(containsString("HttpValidationProblem"));
    }
}
