package io.quarkus.ts.http.restclient.reactive;

import java.net.URI;
import java.net.URL;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.Url;

/**
 * RestClient which provides capability to override base URL.
 * URL might be overridden to access different service.
 * Base URL can be overridden by String, URL or URI parameter type.
 */
@RegisterRestClient
@Path("urlOverride")
public interface UrlOverrideableClient {

    @GET
    @Path("basic")
    String getByString(@Url String uri);

    @GET
    @Path("basic")
    String getByUrl(@Url URL uri);

    @GET
    @Path("basic")
    String getByUri(@Url URI uri);

    @GET
    @Path("basic")
    String getNotOverridden();
}
