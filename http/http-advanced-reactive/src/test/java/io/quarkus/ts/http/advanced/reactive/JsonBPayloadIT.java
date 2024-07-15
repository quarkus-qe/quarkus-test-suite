package io.quarkus.ts.http.advanced.reactive;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class JsonBPayloadIT extends JsonPayloadIT {

    @QuarkusApplication(classes = { FootballTeamResource.class, FootballTeam.class,
            Person.class, PersonResource.class }, properties = "oidcdisable.properties")
    static RestService app = new RestService();

    @Override
    protected RestService getApp() {
        return app;
    }
}
