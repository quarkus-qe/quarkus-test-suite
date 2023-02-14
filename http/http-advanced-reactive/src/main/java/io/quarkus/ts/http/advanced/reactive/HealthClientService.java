package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
public interface HealthClientService {

    @GET
    @Path("/q/health")
    Uni<Response> health();
}
