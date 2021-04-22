package io.quarkus.ts.security.jwt;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/content-types")
public class ContentTypesResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("plain")
    public String plain() {
        return "Hello, world!";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("web")
    public String web() {
        return "<html>Hello, world!</html>";
    }
}
