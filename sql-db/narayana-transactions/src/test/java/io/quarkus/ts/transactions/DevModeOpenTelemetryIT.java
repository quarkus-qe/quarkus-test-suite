package io.quarkus.ts.transactions;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.ts.transactions.TransactionCommons.getTracedOperationsForName;
import static io.quarkus.ts.transactions.TransactionCommons.verifyRequestTraces;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.JaegerContainer;

@QuarkusScenario
public class DevModeOpenTelemetryIT {

    private static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";
    private static final String INSERT_OPERATION_NAME = "INSERT quarkus.journal";
    private static final String UPDATE_OPERATION_NAME = "UPDATE quarkus.account";

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.datasource.jdbc.telemetry", "true")
            .withProperty("quarkus.otel.enabled", "true");

    @Test
    void testJdbcTraces() {
        // verifies that OpenTelemetry JDBC instrumentation works in DEV mode after reload
        // see https://github.com/quarkusio/quarkus/issues/29645 for more information

        // test JDBC tracing is enabled and works
        given().get("/transfer/accounts/ES8521006742088984966816").then().statusCode(HttpStatus.SC_OK);
        verifyRequestTraces("SELECT quarkus.account", jaeger);
        verifyNoTracesForOperation(INSERT_OPERATION_NAME);
        verifyNoTracesForOperation(UPDATE_OPERATION_NAME);

        // disable JDBC tracing and expect no traces are recorded
        app.modifyFile(APPLICATION_PROPERTIES, props -> props + System.lineSeparator() + getOtelEnabledProperty(false));
        untilAsserted(TransactionCommons::makeTopUpTransfer);
        verifyNoTracesForOperation(INSERT_OPERATION_NAME);
        verifyNoTracesForOperation(UPDATE_OPERATION_NAME);

        // enable JDBC tracing and expect new traces
        app.modifyFile(APPLICATION_PROPERTIES, props -> props.replace(
                getOtelEnabledProperty(false), getOtelEnabledProperty(true)));
        untilAsserted(TransactionCommons::makeTopUpTransfer);
        verifyRequestTraces(INSERT_OPERATION_NAME, jaeger);
        verifyRequestTraces(UPDATE_OPERATION_NAME, jaeger);
    }

    private static void verifyNoTracesForOperation(String operationName) {
        var operations = getTracedOperationsForName(operationName, jaeger);
        assertTrue(operations.stream().noneMatch(operationName::equals));
    }

    private static String getOtelEnabledProperty(boolean enabled) {
        return "quarkus.datasource.jdbc.telemetry.enabled=" + Boolean.toString(enabled);
    }

}
