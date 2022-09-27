package io.quarkus.ts.http.hibernate.validator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ReactiveResource;

@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyReactiveUsingTextIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive")
    })
    static final RestService app = new RestService();

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/28324")
    public void validateDefaultMediaType() {
        validate(REACTIVE_ENDPOINT_WITH_NO_PRODUCES)
                .isBadRequest()
                .hasTextError();
    }
}
