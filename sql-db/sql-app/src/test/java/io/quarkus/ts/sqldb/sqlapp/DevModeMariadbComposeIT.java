package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6234")
@QuarkusScenario
public class DevModeMariadbComposeIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication(properties = "mariadb_app.properties")
    static RestService app = new RestService()
            .withProperties(AbstractSqlDatabaseIT::getDockerComposeProperties)
            .withProperty("quarkus.compose.devservices.files", "src/main/resources/mariadb-compose-devservices.yml")
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${mariadb.11.image}");

    @Test
    public void composeDevServicesAreUsed() {
        app.logs().assertContains("Compose is running command");
        app.logs().assertContains(System.getProperty("mariadb.11.image"));
    }
}
