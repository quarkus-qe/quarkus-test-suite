package io.quarkus.ts.security.keycloak.oidcclient.reactive;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
