package io.quarkus.ts.transactions;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.annotations.EnabledIfPostgresImageProduct;

@QuarkusScenario
@EnabledIfPostgresImageProduct
public class PostgresqlProductTransactionGeneralUsageIT extends AbstractPostgresqlTransactionGeneralUsageIT {

    /**
     * Product and community postgresql image has different setup system, so we need to instantiate it differently
     */
    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService().withProperty("PGDATA", "/tmp/psql")
            .withProperty("POSTGRESQL_MAX_PREPARED_TRANSACTIONS", "100");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.jdbc.telemetry", "true")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.datasource.validation-query-timeout.jdbc.url",
                    () -> configureValidationQueryDs(database));

    @Override
    protected RestService getApp() {
        return app;
    }

}
