package io.quarkus.ts.transactions;

import static io.quarkus.test.services.containers.DockerContainerManagedResource.DOCKER_INNER_CONTAINER;

import java.io.IOException;

import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndJava17;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@DisabledOnFipsAndJava17(reason = "https://github.com/quarkusio/quarkus/issues/40813")
@QuarkusScenario
public class MssqlTransactionGeneralUsageIT extends TransactionCommons {

    @SqlServerContainer
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
    protected String[] getExpectedJdbcOperationNames() {
        return new String[] { "SELECT msdb.account", "INSERT msdb.journal", "UPDATE msdb.account" };
    }

}
