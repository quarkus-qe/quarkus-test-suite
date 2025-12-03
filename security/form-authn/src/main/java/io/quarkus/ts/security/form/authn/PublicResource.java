package io.quarkus.ts.security.form.authn;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/public")
public class PublicResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String publicResource() {
        return "public";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String postResource() {
        return "post";
    }
}
