package io.quarkus.ts.transactions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO https://github.com/quarkus-qe/quarkus-test-suite/issues/756
@Tag("fips-incompatible") // native-mode
@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support Linux Containers / Testcontainers (Jaeger)")
public class MysqlTransactionGeneralUsageIT extends TransactionCommons {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.57.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static final MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.otel.tracer.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
