package io.quarkus.ts.transactions;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.test.utils.AwaitilityUtils.untilIsNotNull;
import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.using;
import static io.quarkus.ts.transactions.TransactionCommons.getTracedOperationsForName;
import static io.quarkus.ts.transactions.TransactionCommons.retrieveTraces;
import static io.quarkus.ts.transactions.TransactionCommons.verifyRequestTraces;
import static io.restassured.RestAssured.given;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
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
    private static volatile String previousLiveReloadLogEntry = null;

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
        modifyAppPropertiesAndWait(props -> props + System.lineSeparator() + getOtelEnabledProperty(false));
        untilAsserted(TransactionCommons::makeTopUpTransfer);
        verifyNoTracesForOperation(INSERT_OPERATION_NAME);
        verifyNoTracesForOperation(UPDATE_OPERATION_NAME);

        // enable JDBC tracing and expect new traces
        modifyAppPropertiesAndWait(props -> props.replace(getOtelEnabledProperty(false), getOtelEnabledProperty(true)));
        untilAsserted(TransactionCommons::makeTopUpTransfer);
        verifyRequestTraces(INSERT_OPERATION_NAME, jaeger);
        verifyRequestTraces(UPDATE_OPERATION_NAME, jaeger);
    }

    @Test
    void testSpanContextPropagation() {
        // verifies context propagation with OpenTelemetry
        // please see https://github.com/quarkusio/quarkus/issues/30362 for more details

        var body = given().get("/span/").then().statusCode(HttpStatus.SC_OK).extract().asString();
        assertNotNull(body);
        assertTrue(body.startsWith("Hello "));
        var traceIds = body.substring(6).split("-");
        assertEquals(3, traceIds.length);
        // assert trace ids from all contexts are same
        assertEquals(1, Arrays.stream(traceIds).distinct().count());
        // assert operation is stored in Jaeger under same trace id
        assertEquals(traceIds[0], getTraceIdForSpanOperation());
    }

    private static void verifyNoTracesForOperation(String operationName) {
        var operations = getTracedOperationsForName(operationName, jaeger);
        assertTrue(operations.stream().noneMatch(operationName::equals));
    }

    private static String getTraceIdForSpanOperation() {
        return untilIsNotNull(
                DevModeOpenTelemetryIT::retrieveTraceIdForSpanOperation,
                using(Duration.ofSeconds(2), ofMinutes(1)));
    }

    private static String retrieveTraceIdForSpanOperation() {
        return retrieveTraces(20, "1h", "narayanaTransactions", "GET /span", jaeger).jsonPath().getString("data[0].traceID");
    }

    private static String getOtelEnabledProperty(boolean enabled) {
        return "quarkus.datasource.jdbc.telemetry.enabled=" + enabled;
    }

    private static void modifyAppPropertiesAndWait(Function<String, String> transformProperties) {
        app.modifyFile(APPLICATION_PROPERTIES, transformProperties);

        // TODO: ideally, the Test Framework should take care about waiting when required
        untilAsserted(() -> {
            // just waiting won't do the trick, we need to ping Quarkus as well
            TransactionCommons.makeTopUpTransfer();
            String logEntry = app
                    .getLogs()
                    .stream()
                    .filter(entry -> entry.contains("Live reload total time")
                            && (previousLiveReloadLogEntry == null || !previousLiveReloadLogEntry.equals(entry)))
                    .findAny()
                    .orElse(null);
            if (logEntry != null) {
                previousLiveReloadLogEntry = logEntry;
            } else {
                Assertions.fail();
            }
        });
    }

}
