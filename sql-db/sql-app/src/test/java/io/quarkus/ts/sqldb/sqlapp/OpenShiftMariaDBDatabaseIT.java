package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMariaDBDatabaseIT extends AbstractSqlDatabaseIT {
    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.105.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            // set DB version as we use older version than default version configured at the build time
            .withProperty("quarkus.datasource.db-version", "10.5");
}
