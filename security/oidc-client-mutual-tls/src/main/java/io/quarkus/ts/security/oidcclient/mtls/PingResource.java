package io.quarkus.ts.security.oidcclient.mtls;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
