package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDB10DatabaseIT extends AbstractSqlDatabaseIT {

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.10.image}", port = MARIADB_PORT, expectedLog = "/opt/bitnami/mariadb/sbin/mysqld: ready for connections.")
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
