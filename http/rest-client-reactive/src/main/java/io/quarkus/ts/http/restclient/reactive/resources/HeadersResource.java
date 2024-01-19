package io.quarkus.ts.http.restclient.reactive.resources;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.quarkus.ts.http.restclient.reactive.HeaderClient;

@Path("/headers")
public class HeadersResource {

    private final URI baseUri;

    public HeadersResource(@ConfigProperty(name = "quarkus.http.port") int httpPort) {
        this.baseUri = URI.create("http://localhost:" + httpPort);
    }

    @GET
    @Path("/")
    public String getClientHeader() {
        HeaderClient client = RestClientBuilder
                .newBuilder()
                .baseUri(baseUri)
                .build(HeaderClient.class);

        return client.getClientHeader();
    }
}
