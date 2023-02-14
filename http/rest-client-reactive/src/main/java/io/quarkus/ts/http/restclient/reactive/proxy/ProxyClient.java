package io.quarkus.ts.http.restclient.reactive.proxy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
@RegisterClientHeaders
public interface ProxyClient {

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    Uni<String> getSite();

    @GET
    @Path("/example.txt")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Response> getText();

    @GET
    @Path("/auth")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getAuthorized();
}
