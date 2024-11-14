package io.quarkus.ts.transactions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@QuarkusScenario
public class MariaDbTransactionGeneralUsageIT extends TransactionCommons {

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.10.image}", port = MARIADB_PORT, expectedLog = "socket: '/run/mysqld/mysqld.sock'  port: "
            + MARIADB_PORT)
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected TransactionExecutor getTransactionExecutorUsedForRecovery() {
        return TransactionExecutor.QUARKUS_TRANSACTION;
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/44160")
    @Override
    public void testTransactionRecovery() {
        // TODO: drop this method when https://github.com/quarkusio/quarkus/issues/44160 is fixed
    }
}
