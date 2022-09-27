package io.quarkus.ts.http.advanced.reactive;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
public interface HealthClientService {

    @GET
    @Path("/q/health")
    Uni<Response> health();
}
