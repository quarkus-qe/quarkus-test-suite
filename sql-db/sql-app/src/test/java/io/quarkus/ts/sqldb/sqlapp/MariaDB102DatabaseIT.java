package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDB102DatabaseIT extends AbstractSqlDatabaseIT {

    static final String MARIADB_USER = "user";
    static final String MARIADB_PASSWORD = "user";
    static final String MARIADB_DATABASE = "mydb";
    static final int MARIADB_PORT = 3306;

    @Container(image = "registry.access.redhat.com/rhscl/mariadb-102-rhel7", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static DefaultService database = new DefaultService()
            .withProperty("MYSQL_USER", MARIADB_USER)
            .withProperty("MYSQL_PASSWORD", MARIADB_PASSWORD)
            .withProperty("MYSQL_DATABASE", MARIADB_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties")
            .withProperty("quarkus.datasource.username", MARIADB_USER)
            .withProperty("quarkus.datasource.password", MARIADB_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:mariadb") + ":" + database.getPort() + "/"
                            + MARIADB_DATABASE);
}
