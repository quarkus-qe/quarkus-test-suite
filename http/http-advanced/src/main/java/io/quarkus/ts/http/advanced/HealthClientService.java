package io.quarkus.ts.http.advanced;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface HealthClientService {

    @GET
    @Path("/q/health")
    Response health();
}
