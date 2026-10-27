package io.quarkus.ts.transactions;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.annotations.EnabledIfPostgresImageCommunity;

@QuarkusScenario
@EnabledIfPostgresImageCommunity
public class PostgresqlCommunityTransactionGeneralUsageIT extends AbstractPostgresqlTransactionGeneralUsageIT {

    private static final String ENABLE_PREPARED_TRANSACTIONS = "--max_prepared_transactions=100";

    /**
     * Product and community postgresql image has different setup system, so we need to instantiate it differently.
     */
    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address", command = ENABLE_PREPARED_TRANSACTIONS)
    static final PostgresqlService database = new PostgresqlService().withProperty("PGDATA", "/tmp/psql");

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
