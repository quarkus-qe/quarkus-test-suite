package io.quarkus.ts.security.https;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/hello")
public class HelloResource {

    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSimpleSecurityContext(@Context SecurityContext security) {
        return "Hello, use SSL " + security.isSecure();
    }

    @GET
    @Path("/full")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFullSecurityContext(@Context SecurityContext security) {
        String user = security.getUserPrincipal() != null ? security.getUserPrincipal().getName() : "";
        if ("".equals(user)) {
            user = "<anonymous>";
        }

        return "Hello " + user
                + ", HTTPS: " + security.isSecure()
                + ", isUser: " + security.isUserInRole("user")
                + ", isGuest: " + security.isUserInRole("guest");
    }
}
