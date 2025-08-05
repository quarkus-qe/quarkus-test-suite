package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6234")
@QuarkusScenario
public class DevModePostgresqlComposeIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static RestService app = new RestService()
            .withProperties(AbstractSqlDatabaseIT::getDockerComposeProperties)
            .withProperty("quarkus.compose.devservices.files", "src/main/resources/postgresql-compose-devservices.yml")
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}");

    @Test
    public void composeDevServicesAreUsed() {
        app.logs().assertContains("Compose is running command");
        app.logs().assertContains(System.getProperty("postgresql.latest.image"));
    }
}