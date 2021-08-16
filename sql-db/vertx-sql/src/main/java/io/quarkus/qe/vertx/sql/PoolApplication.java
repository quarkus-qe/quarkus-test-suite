package io.quarkus.qe.vertx.sql;

import java.time.Duration;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;

@RouteBase(path = "/pool")
public class PoolApplication {
    @Inject
    PgPool postgresql;

    @ConfigProperty(name = "quarkus.datasource.reactive.idle-timeout")
    int idle;

    @Route(methods = Route.HttpMethod.GET, path = "/connections")
    public Uni<Long> activeConnections() {
        return postgresql.query(
                "SELECT count(*) as active_con FROM pg_stat_activity where application_name like '%vertx%'")
                .execute().onItem()
                .transform(RowSet::iterator).onItem()
                .transform(iterator -> iterator.hasNext() ? iterator.next().getLong("active_con") : null);
    }

    @Route(methods = Route.HttpMethod.GET, path = "/connect")
    public Uni<Long> newConnection() {
        // Make just one extra query and Hold "Idle + 1 sec" in order to release inactive connections.
        return postgresql.preparedQuery("SELECT CURRENT_TIMESTAMP").execute()
                .onItem().delayIt().by(Duration.ofSeconds(idle + 1))
                .onItem().transformToUni(resp -> activeConnections());
    }
}
