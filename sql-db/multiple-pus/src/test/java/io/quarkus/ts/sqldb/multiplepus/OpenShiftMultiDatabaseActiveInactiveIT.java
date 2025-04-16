package io.quarkus.ts.sqldb.multiplepus;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-5659")
@OpenShiftScenario
public class OpenShiftMultiDatabaseActiveInactiveIT extends AbstractMultiDatabaseActiveInactiveIT {

    @Container(image = "${mariadb.1011.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService mariadb = new MariaDbService();

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("MARIA_DB_USERNAME", mariadb.getUser())
            .withProperty("MARIA_DB_PASSWORD", mariadb.getPassword())
            .withProperty("MARIA_DB_JDBC_URL", mariadb::getJdbcUrl)
            .withProperty("POSTGRESQL_USERNAME", postgresql.getUser())
            .withProperty("POSTGRESQL_PASSWORD", postgresql.getPassword())
            .withProperty("POSTGRESQL_JDBC_URL", postgresql::getJdbcUrl)
            .withProperty("quarkus.datasource.\"fruits\".active", "false")
            .withProperty("quarkus.datasource.\"vegetables\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"fruits\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"vegetables\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"fungi\".active", "false")
            .withProperty("quarkus.datasource.\"fruits\".db-version", "10.11");

    @Override
    protected RestService getApp() {
        return app;
    }
}
