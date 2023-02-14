package io.quarkus.ts.security.oidcclient.mtls;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/ping")
public class PingResource {

    private final SecurityIdentity identity;

    public PingResource(SecurityIdentity identity) {
        this.identity = identity;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> getPrincipalEmail() {
        // HINT: principal claim is email
        return RestResponse.ok(identity.getPrincipal().getName());
    }

}
