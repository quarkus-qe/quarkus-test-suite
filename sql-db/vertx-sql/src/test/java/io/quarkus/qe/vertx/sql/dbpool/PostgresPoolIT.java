package io.quarkus.qe.vertx.sql.dbpool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;

@QuarkusScenario
@TestMethodOrder(OrderAnnotation.class)
public class PostgresPoolIT {
    private static final String POSTGRESQL_DATABASE = "amadeus";

    static final int TIMEOUT_SEC = 60;
    static final int HTTP_OK = 200;
    static WebClient httpClient;

    /*
     * Value is taken from quarkus.datasource.reactive.max-size. Unfortunately, property injection is not supported.
     */
    private static final int DATASOURCE_MAX_SIZE = 5;

    @Container(image = "quay.io/rhoar_qe/postgres", port = 5432, expectedLog = "database system is ready to accept connections")
    static DefaultService postgres = new DefaultService()
            .withProperty("POSTGRES_USER", "test")
            .withProperty("POSTGRES_PASSWORD", "test")
            .withProperty("POSTGRES_DB", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> postgres.getHost().replace("http", "jdbc:postgresql") + ":" +
                            postgres.getPort() + "/" + POSTGRESQL_DATABASE)
            .withProperty("quarkus.datasource.reactive.url",
                    () -> postgres.getHost().replace("http", "postgresql") + ":" +
                            postgres.getPort() + "/" + POSTGRESQL_DATABASE)
            .withProperty("app.selected.db", "postgresql")
            // Enable Flyway for Postgresql
            .withProperty("quarkus.flyway.migrate-at-start", "true")
            // Disable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "false")
            // Disable Flyway for DB2
            .withProperty("quarkus.flyway.db2.migrate-at-start", "false");

    @BeforeAll
    public static void beforeAll() {
        httpClient = WebClient.create(Vertx.vertx(), new WebClientOptions());
    }

    @Test
    @DisplayName("DB connections are re-used")
    @Order(1)
    public void checkDbPoolTurnover() throws InterruptedException {
        final int events = 25000;
        CountDownLatch done = new CountDownLatch(events);

        for (int i = 0; i < events; i++) {
            Uni<Boolean> connectionsReUsed = makeHttpReq(httpClient, "airlines/", HTTP_OK).flatMap(body -> {
                Long activeConnections = activeConnections();
                if (null == activeConnections) {
                    throw new RuntimeException("Oh No! no postgres active connections found!");
                }
                return Uni.createFrom().item(checkDbActiveConnections(activeConnections));
            });

            connectionsReUsed.subscribe().with(reUsed -> {
                assertTrue(reUsed, "More postgres SQL connections than pool max-size property");
                done.countDown();
            });
        }

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(done.getCount(), 0, String.format("Missing %d events.", events - done.getCount()));
    }

    @Test
    @Order(2)
    @DisplayName("IDLE remove expiration time")
    public void checkIdleExpirationTime() throws InterruptedException {
        // push Db pool to the limit in order to raise the number of active connections
        final int events = 25000;
        CountDownLatch done = new CountDownLatch(events);

        for (int i = 0; i < events; i++) {
            Uni<Long> activeConnectionsAmount = makeHttpReq(httpClient, "airlines/", HTTP_OK)
                    .flatMap(body -> {
                        Long activeConnections = activeConnections();
                        if (null == activeConnections) {
                            throw new RuntimeException("Oh No! no postgres active connections found!");
                        }
                        return Uni.createFrom().item(activeConnections);
                    });

            activeConnectionsAmount.subscribe().with(amount -> {
                // be sure that you have more than 1 connections
                assertThat(amount, greaterThanOrEqualTo(1L));
                done.countDown();
            });
        }

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(done.getCount(), 0, String.format("Missing %d events.", events - done.getCount()));

        Long connectionsAmount = addAnotherConection();
        assertEquals(1, connectionsAmount, "Idle doesn't remove IDLE expired connections!.");
    }

    private Long activeConnections() {
        Response response = app.given().get("/pool/connections");
        return response.body().as(Long.class);
    }

    private Long addAnotherConection() {
        Response response = app.given().put("/pool/connect");
        return response.body().as(Long.class);
    }

    protected Uni<JsonArray> makeHttpReq(WebClient httpClient, String path, int expectedStatus) {
        return httpClient.getAbs(getAppEndpoint() + path)
                .expect(ResponsePredicate.status(expectedStatus))
                .send().map(HttpResponse::bodyAsJsonArray);
    }

    protected String getAppEndpoint() {
        return app.getHost() + ":" + app.getPort() + "/";
    }

    private boolean checkDbActiveConnections(long active) {
        return active <= DATASOURCE_MAX_SIZE + (7); // TODO: double check this condition ... this magical number is scary!.
    }
}
