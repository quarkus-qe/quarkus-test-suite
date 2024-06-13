package io.quarkus.ts.opentelemetry;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.JaegerContainer;
import io.restassured.response.Response;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DevModeOpenTelemetryIT {

    private static final Logger LOG = Logger.getLogger(DevModeOpenTelemetryIT.class);
    private static final String APPLICATION_PROPERTIES = Paths.get("src", "main", "resources", "application.properties")
            .toFile()
            .getPath();
    private static final String TRACES_ENABLE_PROPERTY = "quarkus.otel.traces.enabled=";

    private static final int PAGE_LIMIT = 10;
    private static volatile String previousLiveReloadLogEntry = null;

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @DevModeQuarkusApplication(classes = { PingPongService.class, PingResource.class,
            PongResource.class })
    static DevModeQuarkusService otel = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @Test
    @Order(2)
    void checkTraces() {
        String operationName = "GET /hello";
        modifyAppPropertiesAndWait(props -> props.replace(getOtelEnabledProperty(false), getOtelEnabledProperty(true)));
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            doRequest();
            Response response = thenRetrieveTraces(PAGE_LIMIT, "1h", "pingpong", operationName);
            response.then()
                    .body("data.size()", greaterThan(0));
        });
    }

    @Test
    @Order(1)
    void checkThereIsNoTracesAfterRestart() {
        String operationName = "GET /hello";
        modifyAppPropertiesAndWait(props -> props.replace(getOtelEnabledProperty(true), getOtelEnabledProperty(false)));

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            doRequest();
            Response response = thenRetrieveTraces(PAGE_LIMIT, "1h", "pingpong", operationName);
            response.then()
                    .body("data", empty());
        });
    }

    private Response thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        Response response = otel.given()
                .when()
                .queryParam("operation", operationName)
                .queryParam("lookback", lookBack)
                .queryParam("limit", pageLimit)
                .queryParam("service", serviceName)
                .get(jaeger.getTraceUrl());
        LOG.debug("Traces  -->  " + response.asPrettyString());
        return response;
    }

    private static void doRequest() {
        otel.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("pong"));
    }

    private static String getOtelEnabledProperty(boolean enabled) {
        return TRACES_ENABLE_PROPERTY + enabled;
    }

    private static void modifyAppPropertiesAndWait(Function<String, String> transformProperties) {
        otel.modifyFile(APPLICATION_PROPERTIES, transformProperties);
        untilAsserted(() -> {
            // just waiting won't do the trick, we need to ping Quarkus as well
            doRequest();
            String logEntry = otel
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
