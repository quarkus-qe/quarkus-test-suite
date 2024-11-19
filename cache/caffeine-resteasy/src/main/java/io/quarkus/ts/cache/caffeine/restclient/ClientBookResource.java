package io.quarkus.ts.cache.caffeine.restclient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.cache.caffeine.restclient.types.Book;

@Path("/client/book")
public class ClientBookResource {

    @Inject
    @RestClient
    RestInterface restInterface;

    @GET
    @Path("/xml-cache")
    @Produces(MediaType.APPLICATION_XML)
    public Book getAsXmlWithCache() {
        return restInterface.getAsXmlWithCache();
    }

    @GET
    @Path("/json-cache")
    @Produces(MediaType.APPLICATION_JSON)
    public Book getAsJsonWithCache() {
        return restInterface.getAsJsonWithCache();
    }

    @GET
    @Path("/invalidate-xml")
    public String invalidateXml() {
        return restInterface.invalidateXml();
    }

    @GET
    @Path("/invalidate-json")
    public String invalidateJson() {
        return restInterface.invalidateJson();
    }
}
