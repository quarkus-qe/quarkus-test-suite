package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.rar;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.Authenticated;

@Authenticated
@Path("/rar-redirect-wrong")
public class RarRedirectWrongResource {

    @Path("/token-response/authorization-details")
    @GET
    public String getAuthorizationDetails() {
        return "no-op";
    }
}
