package io.quarkus.ts.packaging.treeshake;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

/**
 * Exercises Qute template rendering (generated value resolvers).
 */
@Path("/greeting")
public class GreetingResource {

    @Inject
    Template greeting;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return greeting.data("name", name == null ? "world" : name);
    }
}
