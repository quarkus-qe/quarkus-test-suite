package io.quarkus.ts.transactions;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
public class OracleTransactionGeneralUsageIT extends TransactionCommons {

    private static final int ORACLE_PORT = 1521;
    private static final String DATABASE = "mydb";
    private static final String SEPARATOR = ",";
    /**
     * Create database 'mydb' and 'mydb2' because Oracle prevents 2PC when the same XA resource is detected.
     * When a one transaction is executed on 2 different databases, it is clear that 2PC is required.
     */
    private static final String DATABASES = DATABASE + SEPARATOR + DATABASE + 2;

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService database = new OracleService().withDatabase(DATABASES);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            // we only want the second XA datasource to use 'mydb2', everything else should use 'mydb'
            .withProperty("quarkus.datasource.jdbc.url", () -> database.getJdbcUrl(DATABASE));

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected TransactionExecutor getTransactionExecutorUsedForRecovery() {
        return TransactionExecutor.QUARKUS_TRANSACTION_CALL;
    }

    @Override
    protected Operation[] getExpectedJdbcOperations() {
        return new Operation[] { new Operation("SELECT mydb.dual"), new Operation("INSERT mydb.journal"),
                new Operation("UPDATE mydb.account") };
    }

}
