package io.quarkus.ts.http.hibernate.validator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ReactiveResource;

@Disabled("Wrong MediaType resolution in Resteasy Reactive: https://github.com/quarkusio/quarkus/issues/20888")
@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyReactiveUsingTextIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        assertBadRequestInTextFormat(REACTIVE_ENDPOINT_WITH_NO_PRODUCES);
    }
}
