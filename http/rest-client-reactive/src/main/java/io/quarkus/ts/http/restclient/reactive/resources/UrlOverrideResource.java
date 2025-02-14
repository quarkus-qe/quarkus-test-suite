package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("urlOverride")
public class UrlOverrideResource {

    @ConfigProperty(name = "ts.quarkus.urlOverride.response", defaultValue = "empty")
    String response;

    @GET
    @Path("basic")
    public String basic() {
        return response;
    }
}
