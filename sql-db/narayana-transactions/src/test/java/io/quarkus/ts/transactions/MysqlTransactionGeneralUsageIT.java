package io.quarkus.ts.transactions;

import static io.quarkus.test.services.containers.DockerContainerManagedResource.DOCKER_INNER_CONTAINER;

import java.io.IOException;

import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.transactions.recovery.TransactionExecutor;

@QuarkusScenario
public class MysqlTransactionGeneralUsageIT extends TransactionCommons {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static final MySqlService database = new MySqlService().onPostStart(service -> {
        // enable transactions recovery for user
        var self = (MySqlService) service;
        // RH MySQL image allow login root user locally without using password and ignoring set root password
        String passwordString = System.getProperty("mysql.80.image").contains("redhat") ? "" : "-p" + self.getPassword();
        try {
            self
                    .<GenericContainer<?>> getPropertyFromContext(DOCKER_INNER_CONTAINER)
                    .execInContainer(
                            "/bin/mysql", "-u", "root", passwordString, "-e",
                            "grant XA_RECOVER_ADMIN on *.* to '" + self.getUser() + "'@'%';");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

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
        return TransactionExecutor.INJECTED_TRANSACTION_MANAGER;
    }
}
