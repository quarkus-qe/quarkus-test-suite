package io.quarkus.ts.http.httpproblem;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.httpproblem.sources.CustomProblemPostProcessor;
import io.quarkus.ts.http.httpproblem.sources.HttpProblemResource;
import io.quarkus.ts.http.httpproblem.sources.ValidatedBean;
import io.quarkus.ts.http.httpproblem.sources.ValidationResource;

@Tag("QUARKUS-7309")
@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class HttpProblemResteasyIT extends AbstractHttpProblemIT {

    @QuarkusApplication(classes = { HttpProblemResource.class, ValidationResource.class, ValidatedBean.class,
            CustomProblemPostProcessor.class }, dependencies = {
                    @Dependency(artifactId = "quarkus-resteasy-jackson"),
                    @Dependency(artifactId = "quarkus-hibernate-validator")
            })
    static final RestService app = new RestService();

    @Override
    protected RestService getApp() {
        return app;
    }
}
