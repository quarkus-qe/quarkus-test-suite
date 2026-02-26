package io.quarkus.ts.hibernate.startup.offline.test;

import static org.hamcrest.Matchers.containsStringIgnoringCase;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;

public abstract class AbstractStorageEngineIT {
    @LookupService
    static RestService app;

    @Test
    void pu1UsesMyisamStorageEngine() {
        app.given()
                .get("/storage-engine/pu1/dialect")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("MyISAM"));
    }

    @Test
    void pu2UsesInnodbStorageEngine() {
        app.given()
                .get("/storage-engine/pu2/dialect")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("InnoDB"));
    }
}
