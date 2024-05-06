package io.quarkus.ts.transactions;

import static io.quarkus.test.services.containers.DockerContainerManagedResource.DOCKER_INNER_CONTAINER;

import java.io.IOException;

import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@QuarkusScenario
public class MssqlTransactionGeneralUsageIT extends TransactionCommons {

    private static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService database = new SqlServerService().onPostStart(service -> {
        // enable XA transactions
        var self = (SqlServerService) service;
        try {
            self
                    .<GenericContainer<?>> getPropertyFromContext(DOCKER_INNER_CONTAINER)
                    .execInContainer(
                            "/opt/mssql-tools/bin/sqlcmd", "-S", "localhost", "-U", self.getUser(), "-P", self.getPassword(),
                            "-Q", "EXEC sp_sqljdbc_xa_install");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

    @QuarkusApplication
    public static final RestService app = new RestService().withProperties("mssql.properties")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            // disable encryption as we don't provide trust server certificate etc.
            .withProperty("quarkus.datasource.jdbc.url", () -> database.getJdbcUrl() + ";encrypt=false;");

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected TransactionExecutor getTransactionExecutorUsedForRecovery() {
        return TransactionExecutor.INJECTED_USER_TRANSACTION;
    }

    @Override
    protected boolean isDatabaseTableLockedWhenTransactionFailed() {
        return true;
    }

    @Override
    protected Operation[] getExpectedJdbcOperations() {
        return new Operation[] { new Operation("SELECT msdb.account"), new Operation("INSERT msdb.journal"),
                // here we are looking for UPDATE msdb.ae1_0 because currently DB statement is
                // update ae1_0 set amount=?,updatedAt=? from account ae1_0 where ae1_0.accountNumber=?
                // however we shouldn't rely on alias generation logic, therefore we test the UPDATE statement is there
                new Operation(actualOperationName -> actualOperationName.startsWith("UPDATE msdb.")) };
    }

}
