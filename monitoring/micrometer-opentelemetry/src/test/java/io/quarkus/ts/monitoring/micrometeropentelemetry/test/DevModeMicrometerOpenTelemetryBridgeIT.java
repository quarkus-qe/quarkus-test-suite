package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
public class DevModeMicrometerOpenTelemetryBridgeIT extends AbstractMicrometerOpenTelemetryBridgeIT {

    private static final String DATASOURCE_PROXY_PATH = "/api/datasources/proxy/uid/";
    private static volatile String grafanaEndpoint;

    @DevModeQuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.observability.lgtm.enabled", "true")
            .onPostStart(service -> {
                RestService self = (RestService) service;
                self.logs().assertContains("Dev Service Lgtm started, config");
                grafanaEndpoint = AwaitilityUtils.until(() -> self.given()
                        .get("/observability-dev-service-lgtm/grafana-endpoint")
                        .then().statusCode(200)
                        .extract().body().asString(), Matchers.notNullValue()).toString();
            });

    @AfterAll
    static void tearDown() {
        grafanaEndpoint = null;
    }

    @Override
    protected boolean useBasicAuth() {
        return true;
    }

    @Override
    protected String getLokiUrl() {
        return getGrafanaUrlForDatasource("loki");
    }

    @Override
    protected String getPrometheusUrl() {
        return getGrafanaUrlForDatasource("prometheus");
    }

    @Override
    protected String getTempoUrl() {
        return getGrafanaUrlForDatasource("tempo");
    }

    private static String getGrafanaUrlForDatasource(String datasourceUid) {
        return grafanaEndpoint + DATASOURCE_PROXY_PATH + datasourceUid;
    }
}
