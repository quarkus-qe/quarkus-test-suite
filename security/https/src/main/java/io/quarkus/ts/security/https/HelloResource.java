package io.quarkus.ts.security.https;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

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
        String user = security.getUserPrincipal().getName();
        if ("".equals(user)) {
            user = "<anonymous>";
        }

        return "Hello " + user
                + ", HTTPS: " + security.isSecure()
                + ", isUser: " + security.isUserInRole("user")
                + ", isGuest: " + security.isUserInRole("guest");
    }
}
