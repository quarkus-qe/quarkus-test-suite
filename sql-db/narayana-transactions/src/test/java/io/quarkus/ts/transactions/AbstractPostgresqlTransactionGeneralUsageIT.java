package io.quarkus.ts.transactions;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

public abstract class AbstractPostgresqlTransactionGeneralUsageIT extends TransactionCommons {

    static final int POSTGRESQL_PORT = 5432;

    @Override
    protected TransactionExecutor getTransactionExecutorUsedForRecovery() {
        return TransactionExecutor.TRANSACTIONAL_ANNOTATION;
    }

    protected static RestService createQuarkusApp(PostgresqlService database) {
        return new RestService().withProperties("postgresql.properties")
                .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
                .withProperty("quarkus.datasource.jdbc.telemetry", "true")
                .withProperty("quarkus.datasource.username", database.getUser())
                .withProperty("quarkus.datasource.password", database.getPassword())
                .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
    }
}
