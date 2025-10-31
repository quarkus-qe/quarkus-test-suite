package io.quarkus.ts.vertx.sql;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Query;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

@Singleton
@RouteBase(path = "pool", produces = "text/plain")
public class ManualPool {
    @Inject
    Vertx vertx;

    @ConfigProperty(name = "quarkus.datasource.username")
    String user;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    String url;

    @Route(methods = Route.HttpMethod.GET, path = "/")
    void dynamic(RoutingContext context) {
        SqlConnectOptions sqlConnectOptions = SqlConnectOptions.fromUri(url).setUser(user).setPassword(password);
        Pool pool = Pool.pool(vertx, sqlConnectOptions, new PoolOptions());
        pool.getConnection()
                .flatMap(sqlConnection -> {
                    Query<RowSet<Row>> query = sqlConnection.query("SELECT city FROM amadeus.airports;");
                    return query.execute();
                })
                .onFailure().invoke(context::fail)
                .subscribe().with(cities -> {
                    context.response().end("Total cities: " + cities.size());
                });
    }
}
