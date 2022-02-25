package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.SocketUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMysqlIT extends AbstractSqlDatabaseIT {

    private static final String DB_IMAGE = "quay.io/quarkusqeteam/mysql";
    private static final String DB_VERSION = "8.0";

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", String.format("%s:%s", DB_IMAGE, DB_VERSION));

    @Test
    public void mysqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: quay.io/quarkusqeteam/mysql");
    }
}
