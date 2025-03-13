package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.MetaClient;

@Path("/client/meta")
public class MetaClientResource {

    @Inject
    @RestClient
    MetaClient client;

    @GET
    @Path("/headers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getResource() {
        return client.getHeaders();
    }

}
