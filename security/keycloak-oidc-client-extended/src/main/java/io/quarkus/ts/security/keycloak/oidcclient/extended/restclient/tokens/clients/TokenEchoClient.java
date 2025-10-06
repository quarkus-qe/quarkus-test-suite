package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/secured")
@RegisterProvider(TokenEchoRefreshFilter.class)
public interface TokenEchoClient {

    @GET
    @Path("/echoToken")
    @Produces(MediaType.TEXT_PLAIN)
    String echoToken();
}
