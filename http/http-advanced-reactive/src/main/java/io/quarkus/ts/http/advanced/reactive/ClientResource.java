package io.quarkus.ts.http.advanced.reactive;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/client")
public class ClientResource {
    @Inject
    @RestClient
    HealthClientService client;

    @GET
    public Uni<Response> get() {
        return client.health();
    }
}
