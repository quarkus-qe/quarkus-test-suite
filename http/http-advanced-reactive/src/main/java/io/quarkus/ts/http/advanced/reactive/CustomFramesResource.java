package io.quarkus.ts.http.advanced.reactive;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;

@ApplicationScoped
public class CustomFramesResource {
    protected static final String PING_DATA = "12345678";

    void init(@Observes Router router) {
        router.get("/ping").handler(rc -> {
            rc.request().connection().ping(Buffer.buffer(PING_DATA), event -> {
                rc.response().end(event.result());
            });
        });
    }
}
