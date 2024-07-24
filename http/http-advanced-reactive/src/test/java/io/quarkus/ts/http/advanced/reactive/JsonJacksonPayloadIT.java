package io.quarkus.ts.http.advanced.reactive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class JsonJacksonPayloadIT extends JsonPayloadIT {

    private static final String REST_JACKSON = "rest-jackson";

    @QuarkusApplication(classes = { FootballTeamResource.class, FootballTeam.class, Person.class,
            PersonResource.class }, properties = "oidcdisable.properties", dependencies = @Dependency(artifactId = "quarkus-"
                    + REST_JACKSON))
    static RestService app = new RestService();

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    public void shouldPickJackson() {
        assertTrue(app.logs().forQuarkus().installedFeatures().contains(REST_JACKSON));
    }
}
