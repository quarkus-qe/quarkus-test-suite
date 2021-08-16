package io.quarkus.qe.vertx.sql.dbpool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
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
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;

@QuarkusScenario
@TestMethodOrder(OrderAnnotation.class)
public class PostgresPoolIT {

    private static final String POSTGRESQL_DATABASE = "amadeus";
    private static final int TIMEOUT_SEC = 60;

    /*
     * Value is taken from quarkus.datasource.reactive.max-size. Unfortunately, property injection is not supported.
     */
    private static final int DATASOURCE_MAX_SIZE = 5;

    @Container(image = "${postgresql.10.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static DefaultService postgres = new DefaultService()
            .withProperty("POSTGRESQL_USER", "test")
            .withProperty("POSTGRESQL_PASSWORD", "test")
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);

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

    static WebClient httpClient;

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
            Uni<Boolean> connectionsReUsed = makeHttpReqAsJson(httpClient, "airlines/", HttpStatus.SC_OK)
                    .flatMap(body -> activeConnections()
                            .onItem().ifNull()
                            .failWith(() -> new RuntimeException("Oh No! no postgres active connections found!"))
                            .onItem().ifNotNull().transform(this::checkDbActiveConnections));

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
            Uni<Long> activeConnectionsAmount = makeHttpReqAsJson(httpClient, "airlines/", HttpStatus.SC_OK)
                    .flatMap(body -> activeConnections()
                            .onItem().ifNull()
                            .failWith(() -> new RuntimeException("Oh No! no postgres active connections found!"))
                            .onItem().ifNotNull().transformToUni(resp -> activeConnections()));

            activeConnectionsAmount.subscribe().with(amount -> {
                // be sure that you have more than 1 connections
                assertThat(amount, greaterThanOrEqualTo(1L));
                done.countDown();
            });
        }

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(done.getCount(), 0, String.format("Missing %d events.", events - done.getCount()));

        // Make just one extra query and Hold "Idle + 1 sec" in order to release inactive connections.
        Uni<Long> activeConnectionAmount = selectActiveConnectionsAfterConnection();

        CountDownLatch doneIdleExpired = new CountDownLatch(1);
        activeConnectionAmount.subscribe().with(connectionsAmount -> {
            // At this point you should just have one connection -> SELECT CURRENT_TIMESTAMP
            assertEquals(1, connectionsAmount, "Idle doesn't remove IDLE expired connections!.");
            if (connectionsAmount == 1) {
                doneIdleExpired.countDown();
            }
        });

        doneIdleExpired.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(doneIdleExpired.getCount(), 0, "Missing doneIdleExpired query.");
    }

    private Uni<Long> activeConnections() {
        return makeHttpReqAsLong(httpClient, "/pool/connections", HttpStatus.SC_OK);
    }

    private Uni<Long> selectActiveConnectionsAfterConnection() {
        return makeHttpReqAsLong(httpClient, "/pool/connect", HttpStatus.SC_OK);
    }

    private Uni<Long> makeHttpReqAsLong(WebClient httpClient, String path, int expectedStatus) {
        return makeHttpReq(httpClient, path, expectedStatus).map(HttpResponse::bodyAsString).map(Long::parseLong);
    }

    private Uni<JsonArray> makeHttpReqAsJson(WebClient httpClient, String path, int expectedStatus) {
        return makeHttpReq(httpClient, path, expectedStatus).map(HttpResponse::bodyAsJsonArray);
    }

    private Uni<HttpResponse<Buffer>> makeHttpReq(WebClient httpClient, String path, int expectedStatus) {
        return httpClient.getAbs(getAppEndpoint() + path)
                .expect(ResponsePredicate.status(expectedStatus))
                .send();
    }

    private String getAppEndpoint() {
        return app.getHost() + ":" + app.getPort() + "/";
    }

    private boolean checkDbActiveConnections(long active) {
        return active <= DATASOURCE_MAX_SIZE + (7); // TODO: double check this condition ... this magical number is scary!.
    }
}
