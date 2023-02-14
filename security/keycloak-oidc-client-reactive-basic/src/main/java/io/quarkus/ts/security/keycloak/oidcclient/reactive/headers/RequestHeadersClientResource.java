package io.quarkus.ts.security.keycloak.oidcclient.reactive.headers;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/client-request-headers")
public class RequestHeadersClientResource {

    @Inject
    @RestClient
    RequestHeadersClient client;

    @GET
    @Path("/authorization-repeatedly")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<String>> getAuthorizationHeadersRepeatedly() {
        return client.getAuthorizationHeaders().repeat().atMost(2).collect().last();
    }
}
