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
public class OpenShiftCustomDatasourceProducerIT extends AbstractCustomDatasourceProducerIT {

    @Container(image = "${mariadb.1011.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService mariadb = new MariaDbService();

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("datasource-producer.properties")
            .withProperty("quarkus.datasource.\"mariadb\".username", mariadb::getUser)
            .withProperty("quarkus.datasource.\"mariadb\".password", mariadb::getPassword)
            .withProperty("quarkus.datasource.\"mariadb\".jdbc.url", mariadb::getJdbcUrl)
            .withProperty("quarkus.datasource.\"mariadb\".active", "false")
            .withProperty("quarkus.datasource.\"pg\".username", postgresql::getUser)
            .withProperty("quarkus.datasource.\"pg\".password", postgresql::getPassword)
            .withProperty("quarkus.datasource.\"pg\".jdbc.url", postgresql::getJdbcUrl)
            .withProperty("quarkus.datasource.\"pg\".active", "false")
            .withProperty("quarkus.datasource.\"mariadb\".db-version", "10.11");

    @Override
    protected RestService getApp() {
        return app;
    }
}
