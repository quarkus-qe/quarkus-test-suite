package io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping.clients.ServerSentEventsPongClient;
import io.quarkus.ts.monitoring.opentracing.reactive.grpc.traceable.TraceableResource;
import io.smallrye.mutiny.Multi;

@Path("/server-sent-events-ping")
public class ServerSentEventsPingResource extends TraceableResource {

    @Inject
    @RestClient
    ServerSentEventsPongClient pongClient;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> getPing() {
        recordTraceId();
        return pongClient.getPong().map(response -> "ping " + response);
    }
}