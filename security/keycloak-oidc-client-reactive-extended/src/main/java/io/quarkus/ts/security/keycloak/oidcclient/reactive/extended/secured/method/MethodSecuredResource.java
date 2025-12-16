package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.secured.method;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.Authenticated;

@Path("/method-secured")
public class MethodSecuredResource {

    public static final String PUBLIC_RESPONSE = "publicMethod";
    public static final String SECURED_RESPONSE = "securedMethod";

    @GET
    @Path("/public")
    public String publicMethod() {
        return PUBLIC_RESPONSE;
    }

    @GET
    @Authenticated
    @Path("/secured")
    public String securedMethod() {
        return SECURED_RESPONSE;
    }
}
