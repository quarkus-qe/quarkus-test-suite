package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6234")
@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
public class DevModeMssqlComposeIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication(properties = "mssql.properties")
    static RestService app = new RestService()
            .withProperties(AbstractSqlDatabaseIT::getDockerComposeProperties)
            .withProperty("quarkus.compose.devservices.files", "src/main/resources/mssql-compose-devservices.yml")
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${mssql.image}");

    @Test
    public void composeDevServicesAreUsed() {
        app.logs().assertContains("Compose is running command");
        app.logs().assertContains(System.getProperty("mssql.image"));
    }
}
