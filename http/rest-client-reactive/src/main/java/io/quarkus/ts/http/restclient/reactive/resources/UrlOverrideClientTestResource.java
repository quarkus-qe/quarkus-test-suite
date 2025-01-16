package io.quarkus.ts.http.restclient.reactive.resources;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.UrlOverrideableClient;

@Path("testUrlOverride")
public class UrlOverrideClientTestResource {
    @Inject
    @RestClient
    UrlOverrideableClient urlOverrideableClient;

    @Path("/defaultString")
    @GET
    public String defaultString() {
        return urlOverrideableClient.getByString(null);
    }

    @Path("/overrideString/")
    @GET
    public String overrideString(@QueryParam("port") Integer port) {
        return urlOverrideableClient.getByString(String.format("http://localhost:%d/", port));
    }

    @Path("/defaultUrl")
    @GET
    public String defaultUrl() {
        return urlOverrideableClient.getByUrl(null);
    }

    @Path("/overrideUrl/")
    @GET
    public String overrideUrl(@QueryParam("port") Integer port) throws MalformedURLException {
        return urlOverrideableClient.getByUrl(new URL(String.format("http://localhost:%d/", port)));
    }

    @Path("/defaultUri")
    @GET
    public String defaultUri() {
        return urlOverrideableClient.getByUri(null);
    }

    @Path("/overrideUri/")
    @GET
    public String overrideUri(@QueryParam("port") Integer port) {
        return urlOverrideableClient.getByUri(URI.create(String.format("http://localhost:%d/", port)));
    }

    @GET
    @Path("/notOverridden")
    public String notOverridden() {
        return urlOverrideableClient.getNotOverridden();
    }

    @GET
    @Path("/overrideHost")
    public String overrideHost(@QueryParam("host") String host) {
        return urlOverrideableClient.getByString(String.format("http://" + host));
    }
}
