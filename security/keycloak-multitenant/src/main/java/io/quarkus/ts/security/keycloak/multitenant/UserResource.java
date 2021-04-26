package io.quarkus.ts.security.keycloak.multitenant;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Authenticated
@Path("/user")
@Produces(MediaType.TEXT_PLAIN)
public class UserResource {

    private static final String TENANT_ID_ATTR = "tenant-id";

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/webapp-tenant")
    public String getUsingWebAppTenant() {
        return producesMessage();
    }

    @GET
    @Path("/jwt-tenant")
    public String getUsingJwtTenant() {
        return producesMessage();
    }

    @GET
    @Path("/service-tenant")
    public String getUsingServiceTenant() {
        return producesMessage();
    }

    private String producesMessage() {
        return "Hello, user " + identity.getPrincipal().getName() + " using tenant "
                + identity.getAttributes().get(TENANT_ID_ATTR);
    }
}
