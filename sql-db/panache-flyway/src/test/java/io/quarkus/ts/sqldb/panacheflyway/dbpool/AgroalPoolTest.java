package io.quarkus.ts.sqldb.panacheflyway.dbpool;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.scenarios.annotations.EnabledWhenLinuxContainersAvailable;
import io.quarkus.ts.sqldb.panacheflyway.UserRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

/**
 * The aim of these tests is verified agroal and entityManager pool management
 * Some of these tests required some extra load, in order to reproduce concurrency issues.
 */
@EnabledWhenLinuxContainersAvailable
@QuarkusTest
@TestProfile(AgroalTestProfile.class)
public class AgroalPoolTest {

    private final int CONCURRENCY_LEVEL = 20;
    private final int SAFETY_INTERVAL = 200;

    @Inject
    EntityManager em;

    @Inject
    UserRepository users;

    @Inject
    AgroalDataSource agroalDataSource;

    @TestHTTPResource
    URL url;

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "quarkus.datasource.jdbc.idle-removal-interval")
    String idleSec;

    @ConfigProperty(name = "quarkus.datasource.jdbc.background-validation-interval")
    String idleBackgroundValidationSec;

    @ConfigProperty(name = "quarkus.datasource.jdbc.max-size")
    int datasourceMaxSize;

    @ConfigProperty(name = "quarkus.datasource.jdbc.min-size")
    int datasourceMinSize;

    private static WebClient webClient;

    @BeforeEach
    public void setupClient() {
        if (webClient == null) {
            webClient = WebClient.create(vertx);
        }
    }

    @AfterAll
    public static void closeClient() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    public void idleTimeoutTest() throws InterruptedException {
        makeApplicationQuery();
        Thread.sleep(getIdleMs() + getIdleBackgroundValidationMs() + SAFETY_INTERVAL);
        assertEquals(1, activeConnections(), "agroalCheckIdleTimeout: Expected " + datasourceMinSize + " active connections");
    }

    @Test
    public void poolTurnoverTest() {
        final int events = 500;
        final AtomicInteger max = new AtomicInteger(0);

        AssertSubscriber<Tuple2<Long, Long>> subscriber = Multi.createBy().combining()
                .streams(makeApplicationQueryAsync(events), activeConnectionsAsync(events))
                .asTuple().subscribe().withSubscriber(AssertSubscriber.create(events));

        subscriber.awaitItems(events).getItems().forEach(t -> {
            Long activeCon = t.getItem2();
            if (max.intValue() < activeCon) {
                max.set(activeCon.intValue());
            }
            assertTrue(datasourceMaxSize >= activeCon, "More SQL connections than pool max-size");
            assertTrue(datasourceMinSize <= activeCon, "Less SQL connections than pool min-size");
        });
    }

    @Test
    public void borderConditionBetweenIdleAndGetConnectionTest() {
        final int events = 500;
        for (int k = 0; k < events; k++) {
            AssertSubscriber<Integer> subscriber = Multi.createFrom().range(0, CONCURRENCY_LEVEL).flatMap(n -> Multi
                    .createFrom().ticks()
                    .every(Duration.ofMillis(getIdleMs() + 3))
                    .onOverflow().drop()
                    .onFailure().invoke(e -> Assertions.fail("Unexpected exception " + e.getMessage()))
                    .onItem().transform(i -> makeApplicationQuery()))
                    .subscribe()
                    .withSubscriber(AssertSubscriber.create(CONCURRENCY_LEVEL));

            subscriber
                    .awaitItems(CONCURRENCY_LEVEL)
                    .getItems()
                    .forEach(statusCode -> assertEquals(statusCode, HttpStatus.SC_OK, "Unexpected Application response"));
        }
    }

    @Test
    public void concurrentLoadTest() {
        final int events = 100;
        for (int i = 0; i < events; i++) {
            Multi.createFrom()
                    .range(0, CONCURRENCY_LEVEL).subscribe()
                    .with(n -> assertEquals(2, users.count(), "UnexpectedUser Amount"));
        }
    }

    @Test
    public void connectionConcurrencyTest() {
        final int events = 500;
        for (int k = 0; k < events; k++) {
            AssertSubscriber<String> subscriber = Multi.createFrom().range(0, CONCURRENCY_LEVEL).flatMap(n -> Multi
                    .createFrom().ticks()
                    .every(Duration.ofMillis(getIdleMs() + 3))
                    .onOverflow().drop()
                    .onFailure().invoke(e -> Assertions.fail("Unexpected exception " + e.getMessage()))
                    .onItem().transform(i -> makeAgroalRawQuery()))
                    .subscribe()
                    .withSubscriber(AssertSubscriber.create(CONCURRENCY_LEVEL));

            subscriber
                    .awaitItems(CONCURRENCY_LEVEL)
                    .getItems()
                    .forEach(currentTime -> assertFalse(currentTime.isEmpty(), "Unexpected Application response"));
        }
    }

    private long getIdleMs() {
        float idle = Float.parseFloat(idleSec.replaceAll("[A-Z]", ""));
        return Duration.ofMillis(Math.round(1000 * idle)).toMillis();
    }

    private long getIdleBackgroundValidationMs() {
        float idleBg = Float.parseFloat(idleBackgroundValidationSec.replaceAll("[A-Z]", ""));
        return Duration.ofMillis(Math.round(1000 * idleBg)).toMillis();
    }

    private Multi<Long> activeConnectionsAsync(int events) {
        return Multi.createFrom().range(0, events).onItem().transform(i -> activeConnections());
    }

    private Multi<Long> makeApplicationQueryAsync(int events) {
        return Multi.createFrom().range(0, events).onItem().transform(i -> {
            makeApplicationQuery();
            return 0L;
        });
    }

    private Long activeConnections() {
        Query query = em.createNativeQuery("select * from INFORMATION_SCHEMA.PROCESSLIST;");
        return (long) query.getResultList().size();
    }

    private int makeApplicationQuery() {
        var options = new RequestOptions();
        options.setPort(url.getPort());
        options.setHost(url.getHost());
        options.setMethod(HttpMethod.GET);
        options.setURI("/users/all");
        options.setHeaders(MultiMap.caseInsensitiveMultiMap().add(ACCEPT, "application/hal+json"));
        var response = webClient.request(HttpMethod.GET, options).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        return response.statusCode();
    }

    private String makeAgroalRawQuery() {
        String currentTime = "";
        try (Connection con = agroalDataSource.getConnection();
                Statement statement = con.createStatement();
                ResultSet rs = statement.executeQuery("SELECT CURRENT_TIMESTAMP")) {
            rs.next();
            currentTime = rs.getString(1);
        } catch (SQLException e) {
            assertNull(e.getCause(), "makeAgroalRawQuery: Agroal datasource/poolImpl unexpected error");
        }

        return currentTime;
    }
}
