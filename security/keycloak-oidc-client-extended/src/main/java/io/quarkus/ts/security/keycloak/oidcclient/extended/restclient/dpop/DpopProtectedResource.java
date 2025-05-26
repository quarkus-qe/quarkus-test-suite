package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.dpop;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;

@Path("/dpop")
@Authenticated
public class DpopProtectedResource {

    @Inject
    JsonWebToken principal;

    @GET
    @Produces("text/plain")
    public String hello() {
        return "Hello, " + principal.getName();
    }

    @POST
    @Produces("text/plain")
    public String postHello() {
        return "Hello, " + principal.getName();
    }
}
