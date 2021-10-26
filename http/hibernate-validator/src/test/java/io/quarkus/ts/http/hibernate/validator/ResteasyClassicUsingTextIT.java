package io.quarkus.ts.http.hibernate.validator;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ClassicResource;

@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyClassicUsingTextIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ClassicResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        assertBadRequestInTextFormat(CLASSIC_ENDPOINT_WITH_NO_PRODUCES);
    }
}
