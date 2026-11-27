package io.quarkus.ts.transactions;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftPostgresqlTransactionGeneralUsageIT extends AbstractPostgresqlTransactionGeneralUsageIT {

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService()
            // following env variable is accepted by PG images from Red Hat registry
            .withProperty("POSTGRESQL_MAX_PREPARED_TRANSACTIONS", "100")
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.datasource.jdbc.telemetry", "true")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms");

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    public JaegerService getJaeger() {
        return jaeger;
    }
}
