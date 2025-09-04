package io.quarkus.ts.transactions;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@QuarkusScenario
public class MariaDbTransactionGeneralUsageIT extends TransactionCommons {

    private static final String DATASOURCE_JDBC_ENABLE_RECOVERY = "quarkus.datasource.%s.jdbc.enable-recovery";
    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.11.image}", port = MARIADB_PORT, expectedLog = "socket: '.*/mysql.*sock'  port: "
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

    @Tag("QUARKUS-5709")
    @Override
    protected void enableTransactionRecovery() {
        app.withProperty(getDsJdbcEnableRecovery("xa-ds-1"), Boolean.TRUE.toString());
        app.withProperty(getDsJdbcEnableRecovery("xa-ds-2"), Boolean.TRUE.toString());
    }

    @Tag("QUARKUS-5709")
    @Override
    protected void disableTransactionRecovery() {
        app.withProperty(getDsJdbcEnableRecovery("xa-ds-1"), Boolean.FALSE.toString());
        app.withProperty(getDsJdbcEnableRecovery("xa-ds-2"), Boolean.FALSE.toString());
    }

    private String getDsJdbcEnableRecovery(String datasourceName) {
        return DATASOURCE_JDBC_ENABLE_RECOVERY.formatted(datasourceName);
    }
}
