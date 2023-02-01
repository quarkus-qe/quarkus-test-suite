package io.quarkus.ts.transactions;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support Linux Containers / Testcontainers (Jaeger)")
public class MariaDbTransactionGeneralUsageIT extends TransactionCommons {

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.10.image}", port = MARIADB_PORT, expectedLog = "socket: '/run/mysqld/mysqld.sock'  port: "
            + MARIADB_PORT)
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties")
            .withProperty("quarkus.opentelemetry.tracer.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
