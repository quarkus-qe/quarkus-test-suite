package io.quarkus.ts.security.keycloak.oidcclient.reactive.headers;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/request-headers")
public class RequestHeadersResource {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @GET
    @Path("/authorization")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<String>> getAuthorizationHeaders(HttpHeaders httpHeaders) {
        return Uni.createFrom().item(httpHeaders.getRequestHeader(AUTHORIZATION_HEADER));
    }
}
