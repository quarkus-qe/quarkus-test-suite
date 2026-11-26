package io.quarkus.ts.hibernate.startup.offline.test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // using order only to test the traces last
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class OpenTelemetryHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    // Jaeger v3 API requires a time window (v1 API did not)
    String startMin = Instant.now().minus(1, ChronoUnit.HOURS).toString();
    String startMax = Instant.now().plus(1, ChronoUnit.HOURS).toString();

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address", builder = FixedPortResourceBuilder.class)
    static final PostgresqlService db = new PostgresqlService().setAutoStart(false);

    @QuarkusApplication(dependencies = {
            @Dependency(artifactId = "quarkus-jdbc-postgresql"),
            @Dependency(artifactId = "quarkus-opentelemetry")
    })
    static final RestService app = new RestService()
            .withProperty("quarkus.application.name", "app")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.telemetry", "true")
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.telemetry", "true")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms")
            .withProperty("jdbc-url", "jdbc:postgresql://localhost:5432/mydb");

    @Order(Integer.MAX_VALUE) // run last so that JDBC traces produced by other test methods are ready
    @Test
    void testJdbcTraces() {
        AwaitilityUtils.untilAsserted(() -> {
            var response = thenRetrieveTraces();
            var operationNames = response.jsonPath().getList("result.resourceSpans.scopeSpans.spans.name.flatten()",
                    String.class);
            assertThat(operationNames).contains("SELECT mydb.Article").contains("INSERT mydb.Article");
            var tagValues = response.jsonPath()
                    .getList("result.resourceSpans.scopeSpans.spans.attributes.value.stringValue.flatten()");
            assertThat(tagValues)
                    .contains("create schema if not exists req_scope_credentials")
                    .contains("create schema if not exists app_scope_credentials");
        });
    }

    private Response thenRetrieveTraces() {
        return given().when()
                .queryParam("lookback", "1h")
                .queryParam("limit", 10000)
                .queryParam("query.service_name", "app")
                .queryParam("query.start_time_min", startMin)
                .queryParam("query.start_time_max", startMax)
                .get(jaeger.getTraceV3Url())
                .then().statusCode(200)
                .extract().response();
    }

}
