package io.quarkus.ts.security.vertx.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.ts.security.vertx.model.BladeRunner;
import io.quarkus.ts.security.vertx.services.BladeRunnerService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class BladeRunnerHandler {

    @Inject
    BladeRunnerService bladeRunnerService;

    public void upsertBladeRunner(final RoutingContext context) {
        BladeRunner bladeRunner = context.body().asJsonObject().mapTo(BladeRunner.class);
        bladeRunnerService.upsert(bladeRunner)
                .onFailure().invoke(context::fail)
                .subscribe().with(success -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("id", bladeRunner.getId()).encode()));
    }

    public void deleteBladeRunner(final RoutingContext context) {
        String id = context.request().getParam("id");
        bladeRunnerService.delete(id).onFailure().invoke(context::fail)
                .subscribe().with(success -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(NO_CONTENT.code()).end());
    }

    public void getBladeRunnerById(final RoutingContext context) {
        String id = context.request().getParam("id");
        bladeRunnerService.get(id).onFailure().invoke(context::fail)
                .subscribe().with(bladeRunner -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(bladeRunner.toJsonEncoded()));
    }

    public void getAllBladeRunner(final RoutingContext context) {
        bladeRunnerService.get().onFailure().invoke(context::fail)
                .subscribe().with(bladeRunners -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encode(bladeRunners)));
    }
}
