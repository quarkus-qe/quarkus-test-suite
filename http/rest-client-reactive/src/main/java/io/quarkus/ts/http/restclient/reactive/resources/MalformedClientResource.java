package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.MalformedClient;

@Path("/client/malformed")
public class MalformedClientResource {
    @Inject
    @RestClient
    MalformedClient client;

    @GET
    @Path("/")
    public String getMalformed() {
        try {
            return client.get();
        } catch (Exception ex) {
            return ex.getClass().getName();
        }
    }
}
