package io.quarkus.ts.transactions;

import static io.quarkus.test.services.containers.DockerContainerManagedResource.DOCKER_INNER_CONTAINER;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;
import io.restassured.response.Response;

@Tag("fips-incompatible") // MSSQL works with BC JSSE FIPS which is not native-compatible, we test FIPS elsewhere
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@QuarkusScenario
public class MssqlTransactionGeneralUsageIT extends TransactionCommons {
    private static final Logger LOG = Logger.getLogger(MssqlTransactionGeneralUsageIT.class);

    @SqlServerContainer
    static SqlServerService database = new SqlServerService().onPostStart(service -> {
        // enable XA transactions
        var self = (SqlServerService) service;
        try {
            self
                    .<GenericContainer<?>> getPropertyFromContext(DOCKER_INNER_CONTAINER)
                    .execInContainer(
                            "/opt/mssql-tools18/bin/sqlcmd", "-C", "-S", "localhost", "-U", self.getUser(),
                            "-P", self.getPassword(), "-Q", "EXEC sp_sqljdbc_xa_install");
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

    @Test
    @Tag("long-running")
    @Tag("QUARKUS-4185")
    //on average, there is one leaking connection every 2 minutes
    void connectionLeak() throws InterruptedException {
        int before = getConnections();
        Duration later = Duration.of(4L, ChronoUnit.MINUTES).plus(Duration.ofSeconds(15));
        LOG.warn("Waiting for " + later.toSeconds() + " seconds to check for leaking connections");
        Thread.sleep(later.toMillis());
        int after = getConnections();
        Assertions.assertFalse(after - before > 1, //single additional connection may be open temporary
                "Connections are leaking, was: " + before + " now: " + after);

    }

    private int getConnections() {
        Response response = getApp().given().get("/service/connections");
        Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
        return Integer.parseInt(response.asString());
    }
}
