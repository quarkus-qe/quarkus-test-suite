package io.quarkus.ts.sqldb.multiplepus;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMultiplePersistenceIT extends AbstractMultiplePersistenceIT {

    static final int MYSQL_PORT = 3306;

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${mariadb.103.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService mariadb = new MariaDbService();

    // FIXME: change expected log when https://github.com/quarkus-qe/quarkus-test-framework/issues/1183 is fixed
    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "Future log output will appear in directory")
    static PostgresqlService postgresql = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("MARIA_DB_USERNAME", mariadb.getUser())
            .withProperty("MARIA_DB_PASSWORD", mariadb.getPassword())
            .withProperty("MARIA_DB_JDBC_URL", mariadb::getJdbcUrl)
            .withProperty("POSTGRESQL_USERNAME", postgresql.getUser())
            .withProperty("POSTGRESQL_PASSWORD", postgresql.getPassword())
            .withProperty("POSTGRESQL_JDBC_URL", postgresql::getJdbcUrl);

    @Override
    RestService getApp() {
        return app;
    }
}
