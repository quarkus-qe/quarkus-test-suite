package io.quarkus.ts.security.jwt;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
