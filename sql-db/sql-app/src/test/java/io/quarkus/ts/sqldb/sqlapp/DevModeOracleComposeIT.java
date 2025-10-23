package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6234")
@Tag("podman-incompatible") // TODO: https://github.com/quarkusio/quarkus/issues/38003
@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
// This issue (ignoring extended startup timeout) affects both windows and linux.
// but only on windows it causes tests to fail. Linux can make consistently it during default timeout.
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkusio/quarkus/issues/50689")
public class DevModeOracleComposeIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication(properties = "oracle.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.compose.devservices.files", "src/main/resources/oracle-compose-devservices.yml")
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${oracle.image}");

    @Test
    public void composeDevServicesAreUsed() {
        app.logs().assertContains("Compose is running command");
        app.logs().assertContains(System.getProperty("oracle.image"));
    }
}
