package io.quarkus.ts.http.advanced.reactive.clients;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface HealthClientService {

    @GET
    @Path("/health")
    Response health();
}
