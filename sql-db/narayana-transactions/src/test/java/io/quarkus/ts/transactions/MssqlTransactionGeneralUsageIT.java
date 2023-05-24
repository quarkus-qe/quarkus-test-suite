package io.quarkus.ts.transactions;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support Linux Containers / Testcontainers (Jaeger)")
public class MssqlTransactionGeneralUsageIT extends TransactionCommons {

    private static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    public static final RestService app = new RestService().withProperties("mssql.properties")
            .withProperty("quarkus.otel.tracer.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
