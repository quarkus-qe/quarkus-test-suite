package io.quarkus.ts.http.advanced.reactive.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface HttpVersionClientService {

    @GET
    @Path("/httpVersion")
    Response getClientHttpVersion();
}
