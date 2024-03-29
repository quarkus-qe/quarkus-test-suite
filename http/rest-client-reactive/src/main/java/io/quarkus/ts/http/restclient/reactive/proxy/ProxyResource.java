package io.quarkus.ts.http.restclient.reactive.proxy;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/proxied")
public class ProxyResource {

    @Inject
    @RestClient
    ProxyClient client;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> getRoot() {
        return client.getSite();
    }

    @GET
    @Path("/banned")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> getBanned() {
        return client.getText();
    }

    @GET
    @Path("/authorization")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getAuthorized() {
        return client.getAuthorized();
    }

}
