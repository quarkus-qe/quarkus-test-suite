package io.quarkus.qe;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.ManagementInterface;

public class ManagementRoute {

    void setupCustomManagementRoute(@Observes ManagementInterface mi) {
        mi.router().route("/management-ping").handler(ctx -> ctx.response().end("pong"));
    }

}
