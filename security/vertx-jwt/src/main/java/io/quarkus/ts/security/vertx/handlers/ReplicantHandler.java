package io.quarkus.ts.security.vertx.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.ts.security.vertx.model.Replicant;
import io.quarkus.ts.security.vertx.services.ReplicantService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class ReplicantHandler {

    @Inject
    ReplicantService replicantService;

    public void upsertReplicant(final RoutingContext context) {
        Replicant replicant = context.body().asJsonObject().mapTo(Replicant.class);
        replicantService.upsert(replicant)
                .onFailure().invoke(context::fail)
                .subscribe().with(success -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("id", replicant.getId()).encode()));
    }

    public void deleteReplicant(final RoutingContext context) {
        String id = context.request().getParam("id");
        replicantService.delete(id).onFailure().invoke(context::fail)
                .subscribe().with(success -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(NO_CONTENT.code()).end());
    }

    public void getReplicantById(final RoutingContext context) {
        String id = context.request().getParam("id");
        replicantService.get(id).onFailure().invoke(context::fail)
                .subscribe().with(replicant -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(replicant.toJsonEncoded()));
    }

    public void getAllReplicant(final RoutingContext context) {
        replicantService.get().onFailure().invoke(context::fail)
                .subscribe().with(replicants -> context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encode(replicants)));
    }
}
