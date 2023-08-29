package io.quarkus.ts.transactions;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMysqlTransactionGeneralUsageIT extends TransactionCommons {
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
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
        return TransactionExecutor.STATIC_USER_TRANSACTION;
    }

    @Override
    protected void testTransactionRecoveryInternal() {
        // disabled on OpenShift as there are required changes in Test Framework in container restart
    }
}
