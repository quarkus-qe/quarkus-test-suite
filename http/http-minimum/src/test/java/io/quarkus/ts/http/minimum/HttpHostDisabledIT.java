package io.quarkus.ts.http.minimum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-1561")
@QuarkusScenario
public class HttpHostDisabledIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperty("quarkus.http.host-enabled", "false");

    @Test
    public void ensureApplicationStartsWithHostDisable() {
        Assertions.assertTrue(app.isRunning(), "Application should start with quarkus.http.host-enabled=false");
        app.logs().assertDoesNotContain("Must configure at least one of http, https or unix domain socket");
    }
}
