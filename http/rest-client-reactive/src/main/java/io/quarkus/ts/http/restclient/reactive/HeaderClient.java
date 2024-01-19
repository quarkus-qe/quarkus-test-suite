package io.quarkus.ts.http.restclient.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/headersReflection")
public interface HeaderClient {

    @GET
    @Path("/client")
    String getClientHeader();
}
