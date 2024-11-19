package io.quarkus.ts.cache.caffeine.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.ts.cache.caffeine.restclient.types.Book;

@RegisterRestClient
@Path("/book")
@RegisterClientHeaders
public interface RestInterface {

    @GET
    @Path("/xml-cache")
    @CacheResult(cacheName = "xml")
    @Produces(MediaType.APPLICATION_XML)
    Book getAsXmlWithCache();

    @GET
    @Path("/json-cache")
    @CacheResult(cacheName = "json")
    @Produces(MediaType.APPLICATION_JSON)
    Book getAsJsonWithCache();

    @GET
    @Path("/xml-cache-invalidate")
    @Produces(MediaType.TEXT_PLAIN)
    @CacheInvalidateAll(cacheName = "xml")
    String invalidateXml();

    @GET
    @Path("/json-cache-invalidate")
    @CacheInvalidate(cacheName = "json")
    String invalidateJson();
}
