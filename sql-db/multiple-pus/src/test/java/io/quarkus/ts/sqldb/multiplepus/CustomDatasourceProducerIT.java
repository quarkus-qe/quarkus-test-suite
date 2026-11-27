package io.quarkus.ts.sqldb.multiplepus;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-5659")
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class CustomDatasourceProducerIT extends AbstractCustomDatasourceProducerIT {

    @Container(image = "${mariadb.12.image}", port = MARIADB_PORT, expectedLog = "socket: '.*sock'  port: " + MARIADB_PORT)
    static MariaDbService mariadb = new MariaDbService();

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("datasource-producer.properties")
            .withProperty("quarkus.datasource.\"mariadb\".username", mariadb::getUser)
            .withProperty("quarkus.datasource.\"mariadb\".password", mariadb::getPassword)
            .withProperty("quarkus.datasource.\"mariadb\".jdbc.url", mariadb::getJdbcUrl)
            .withProperty("quarkus.datasource.\"mariadb\".active", "false")
            .withProperty("quarkus.datasource.\"pg\".username", postgresql::getUser)
            .withProperty("quarkus.datasource.\"pg\".password", postgresql::getPassword)
            .withProperty("quarkus.datasource.\"pg\".jdbc.url", postgresql::getJdbcUrl)
            .withProperty("quarkus.datasource.\"pg\".active", "false");

    @Override
    protected RestService getApp() {
        return app;
    }
}
