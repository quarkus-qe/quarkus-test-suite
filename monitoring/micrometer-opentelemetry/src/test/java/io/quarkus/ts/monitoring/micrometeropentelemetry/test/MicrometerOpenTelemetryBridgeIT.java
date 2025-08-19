package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import io.quarkus.test.bootstrap.GrafanaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.GrafanaContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MicrometerOpenTelemetryBridgeIT extends AbstractMicrometerOpenTelemetryBridgeIT {

    @GrafanaContainer
    static final GrafanaService grafana = new GrafanaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", grafana::getOtlpCollectorUrl)
            .withProperty("quarkus.otel.exporter.otlp.logs.endpoint", grafana::getOtlpCollectorUrl)
            .withProperty("quarkus.otel.exporter.otlp.metrics.endpoint", grafana::getOtlpCollectorUrl);

    @Override
    protected boolean useBasicAuth() {
        return false;
    }

    @Override
    protected String getLokiUrl() {
        return grafana.getLokiUrl();
    }

    @Override
    protected String getTempoUrl() {
        return grafana.getTempoUrl();
    }

    @Override
    protected String getPrometheusUrl() {
        return grafana.getPrometheusUrl();
    }
}
