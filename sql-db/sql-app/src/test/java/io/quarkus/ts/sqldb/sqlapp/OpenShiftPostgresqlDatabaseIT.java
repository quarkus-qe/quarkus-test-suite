package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftPostgresqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
