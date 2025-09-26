package io.quarkus.ts.hibernate.startup.offline.test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // using order only to test the traces last
@QuarkusScenario
public class OpenTelemetryHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
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
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("jdbc-url", "jdbc:postgresql://localhost:5432/mydb");

    @Order(Integer.MAX_VALUE) // run last so that JDBC traces produced by other test methods are ready
    @Test
    void testJdbcTraces() {
        AwaitilityUtils.untilAsserted(() -> {
            var response = thenRetrieveTraces();
            var operationNames = response.jsonPath().getList("data.spans.operationName.flatten()", String.class);
            assertThat(operationNames).contains("SELECT mydb.Article").contains("INSERT mydb.Article");
            var tagValues = response.jsonPath().getList("data.flatten().spans.flatten().tags.flatten().value");
            assertThat(tagValues)
                    .contains("create schema req_scope_credentials")
                    .contains("create schema app_scope_credentials");
        });
    }

    private static Response thenRetrieveTraces() {
        return given().when()
                .queryParam("lookback", "1h")
                .queryParam("limit", 10000)
                .queryParam("service", "app")
                .get(jaeger.getTraceUrl())
                .then().statusCode(200)
                .extract().response();
    }

}
