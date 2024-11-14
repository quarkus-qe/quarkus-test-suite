package io.quarkus.ts.messaging.kafka.producer;

import jakarta.enterprise.context.ApplicationScoped;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

@ApplicationScoped
public class FailureHandler {

    public void handler(final RoutingContext ctx) {
        JsonObject error = defaultError(ctx.normalizedPath());

        if (ctx.failure() instanceof HttpException httpExp) {
            error.put("status", httpExp.getStatusCode());
        }

        if (ctx.failure().getMessage() != null) {
            error.put("message", ctx.failure().getMessage());
        }

        ctx.response().setStatusCode(error.getInteger("status"));
        ctx.response().end(error.encode());
    }

    private JsonObject defaultError(String path) {
        return new JsonObject()
                .put("timestamp", System.currentTimeMillis())
                .put("status", HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .put("error", HttpResponseStatus.valueOf(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).reasonPhrase())
                .put("path", path);
    }
}
