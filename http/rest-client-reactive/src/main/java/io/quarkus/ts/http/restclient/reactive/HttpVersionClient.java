package io.quarkus.ts.http.restclient.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
@Path("/http-version")
public interface HttpVersionClient {

    @GET
    @Path("synchronous")
    Response getClientHttpVersion(@QueryParam("name") String name);

    @GET
    @Path("asynchronous")
    Uni<Response> getClientHttpVersionAsync(@QueryParam("name") String name);
}
