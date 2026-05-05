package io.quarkus.ts.sqldb.sqlapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-7644")
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
public class OracleConnectionRollbackIT {

    private static final String INSERT_LOG = OracleRollbackService.INSERT_EXECUTED_LOG;

    static final int ORACLE_PORT = 1521;

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService database = new OracleService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("oracle.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.shutdown.timeout", "5s");

    @Test
    public void verifyInFlightTransactionRolledBackOnShutdown() throws Exception {
        app.given().post("/oracle-rollback/init")
                .then().statusCode(HttpStatus.SC_OK);

        app.given().post("/oracle-rollback/trigger")
                .then().statusCode(HttpStatus.SC_ACCEPTED);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> app.getLogs().stream().anyMatch(line -> line.contains(INSERT_LOG)));

        app.stop();

        try (Connection conn = DriverManager.getConnection(
                database.getJdbcUrl(), database.getUser(), database.getPassword())) {
            try (ResultSet rs = conn.prepareStatement("SELECT COUNT(*) FROM rollback_test").executeQuery()) {
                rs.next();
                assertEquals(0, rs.getInt(1),
                        "In-flight transaction should be rolled back on shutdown, "
                                + "but rows were committed (Oracle implicit commit on close)");
            }
        }
    }
}
