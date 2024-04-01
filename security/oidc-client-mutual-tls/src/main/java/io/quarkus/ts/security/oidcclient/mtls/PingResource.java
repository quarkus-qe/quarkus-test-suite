package io.quarkus.ts.security.oidcclient.mtls;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/ping")
public class PingResource {

    private final SecurityIdentity identity;

    public PingResource(SecurityIdentity identity) {
        this.identity = identity;
    }

    @Path("/oidc")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getOidcPrincipalEmail() {
        // HINT: principal claim is email
        return RestResponse.ok(identity.getPrincipal().getName());
    }

    @Path("/mtls/authentication")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getMtlsAuthentication() {
        return RestResponse.ok(identity.getPrincipal().getName());
    }

    @Path("/mtls/roles-policy")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getMtlsRolesPolicy() {
        return RestResponse.ok(identity.getPrincipal().getName());
    }

    @PermissionsAllowed("get")
    @Path("/mtls/permissions-allowed")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getMtlsPermissionsAllowed() {
        return RestResponse.ok(identity.getPrincipal().getName());
    }

}
