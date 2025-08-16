package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import io.quarkus.test.bootstrap.GrafanaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.GrafanaContainer;

@QuarkusScenario
public class DevModeMicrometerOpenTelemetryBridgeIT extends AbstractMicrometerOpenTelemetryBridgeIT {

    // TODO: use Observability LGTM Dev Services when https://github.com/quarkusio/quarkus/issues/49571 is fixed
    //   add quarkus-observability-devservices-lgtm and remove the QE Grafana container
    @GrafanaContainer
    static final GrafanaService grafana = new GrafanaService();

    @DevModeQuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", grafana::getOtlpCollectorUrl)
            .withProperty("quarkus.otel.exporter.otlp.logs.endpoint", grafana::getOtlpCollectorUrl)
            .withProperty("quarkus.otel.exporter.otlp.metrics.endpoint", grafana::getOtlpCollectorUrl);

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
