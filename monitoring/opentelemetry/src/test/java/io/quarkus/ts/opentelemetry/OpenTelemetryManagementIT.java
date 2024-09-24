package io.quarkus.ts.opentelemetry;

import static io.quarkus.test.services.containers.JaegerGenericDockerContainerManagedResource.CERTIFICATE_CONTEXT_KEY;
import static io.quarkus.test.services.containers.JaegerGenericDockerContainerManagedResource.JAEGER_CLIENT_CERT_CN;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.Certificate;
import io.quarkus.test.security.certificate.PemClientCertificate;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-4592")
@QuarkusScenario
public class OpenTelemetryManagementIT {
    @JaegerContainer(tls = true, builder = JaegerLocalhostDockerManagerResource.class)
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService pong = new RestService()
            .withProperty("quarkus.application.name", "pong")
            .withProperty("quarkus.management.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", () -> jaeger.getCollectorUrl(Protocol.HTTPS))
            .withProperty("quarkus.otel.exporter.otlp.traces.tls-configuration-name", "jaeger")
            .withProperty("quarkus.tls.jaeger.key-store.pem.0.cert", OpenTelemetryManagementIT::getTlsCertPath)
            .withProperty("quarkus.tls.jaeger.key-store.pem.0.key", OpenTelemetryManagementIT::getTlsKeyPath)
            .withProperty("quarkus.tls.jaeger.trust-store.pem.certs", OpenTelemetryManagementIT::getTlsCaCertPath);

    private static final String PONG_ENDPOINT = "/hello";
    private static final String MANAGEMENT_ENDPOINT = "/q/health/ready";

    /**
     * Test openTelemetry not sending traces from management endpoints
     */
    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/37218")
    public void managementEndpointExcludedFromTracesTest() {
        // invoke management endpoint, so service is prone to send trace from it
        // if fix is already in place, it should not send any
        pong.management().get(MANAGEMENT_ENDPOINT)
                .then().statusCode(HttpStatus.SC_OK);

        // invoke normal endpoint, so we can check that traces are uploading correctly
        given()
                .when().get(PONG_ENDPOINT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("pong"));

        // wait for pong endpoint to be logged in traces
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> given()
                .when()
                .queryParam("service", pong.getName())
                .get(jaeger.getTraceUrl())
                .then().statusCode(HttpStatus.SC_OK)
                .and().body(containsString(PONG_ENDPOINT)));

        String traces = given().when()
                .queryParam("service", pong.getName())
                .get(jaeger.getTraceUrl())
                .thenReturn().body().asString();

        // check that management endpoint is not present in traces, while correct trace is there
        Assertions.assertTrue(traces.contains(PONG_ENDPOINT), "Pong endpoint should be logged in traces");
        Assertions.assertFalse(traces.contains(MANAGEMENT_ENDPOINT), "Management endpoint should not be logged in traces");
    }

    private static String getTlsKeyPath() {
        return addEscapes(getClientCertificate().keyPath());
    }

    private static String getTlsCertPath() {
        return addEscapes(getClientCertificate().certPath());
    }

    private static String getTlsCaCertPath() {
        return addEscapes(getClientCertificate().truststorePath());
    }

    private static PemClientCertificate getClientCertificate() {
        return (PemClientCertificate) jaeger.<Certificate> getPropertyFromContext(CERTIFICATE_CONTEXT_KEY)
                .getClientCertificateByCn(JAEGER_CLIENT_CERT_CN);
    }

    static String addEscapes(String path) {
        if (OS.WINDOWS.isCurrentOs()) {
            // TODO: move this to the FW
            // back-slashes have special meaning in Cygwin etc.
            return path.replace("\\", "\\\\");
        }
        return path;
    }
}
