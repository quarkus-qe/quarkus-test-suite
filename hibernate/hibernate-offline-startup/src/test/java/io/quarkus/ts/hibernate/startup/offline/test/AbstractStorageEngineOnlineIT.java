package io.quarkus.ts.hibernate.startup.offline.test;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;

public abstract class AbstractStorageEngineOnlineIT {

    @LookupService
    static RestService app;

    @Test
    void pu1PhysicalTableUsesMyIsam() {
        app.given()
                .get("/storage-engine/pu1/table-ddl")
                .then()
                .statusCode(200)
                .body(containsString("ENGINE=MyISAM"));
    }

    @Test
    void pu2PhysicalTableUsesInnodb() {
        app.given()
                .get("/storage-engine/pu2/table-ddl")
                .then()
                .statusCode(200)
                .body(containsString("ENGINE=InnoDB"));
    }
}
