package io.quarkus.ts.vertx;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("https://github.com/quarkusio/quarkus/discussions/47481")
@QuarkusScenario
public class MdcContextPropagationIT {

    @BeforeEach
    public void clearLogs() {
        InMemoryLogHandler.reset();
    }

    @RepeatedTest(5)
    void testMdcIsPropagatedFromExternalEndpointToHealthCheck() {
        given()
                .when()
                .get("/external-health")
                .then()
                .statusCode(200);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> records = given()
                    .when()
                    .get("/external-health/log-records")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath()
                    .getList(".", String.class);

            assertThat("Should have at least 2 log records", records.size(), greaterThanOrEqualTo(2));

            boolean hasHealthCheck1Log = records.stream()
                    .anyMatch(record -> record.contains("endpoint_context=value-from-endpoint-MdcPropagationHealthCheck1")
                            && record.contains("Test log with MDC"));

            boolean hasHealthCheck2Log = records.stream()
                    .anyMatch(record -> record.contains("endpoint_context=value-from-endpoint-MdcPropagationHealthCheck2")
                            && record.contains("Test log with MDC"));

            assertThat("Should have log from MdcPropagationHealthCheck1 with correct MDC",
                    hasHealthCheck1Log, is(true));
            assertThat("Should have log from MdcPropagationHealthCheck2 with correct MDC",
                    hasHealthCheck2Log, is(true));

            records.forEach(record -> {
                if (record.contains("MdcPropagationHealthCheck1")) {
                    assertThat("HC1 log should not contain HC2 context",
                            record.contains("MdcPropagationHealthCheck2"), is(false));
                }
                if (record.contains("MdcPropagationHealthCheck2")) {
                    assertThat("HC2 log should not contain HC1 context",
                            record.contains("MdcPropagationHealthCheck1"), is(false));
                }
            });
        });
    }

}