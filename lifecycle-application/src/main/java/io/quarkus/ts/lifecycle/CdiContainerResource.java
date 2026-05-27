package io.quarkus.ts.lifecycle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.arc.Arc;

@Path("/cdi")
public class CdiContainerResource {

    @GET
    @Path("/container-status")
    @Produces(MediaType.TEXT_PLAIN)
    public String containerStatus() {
        return Arc.container() != null ? "active" : null;
    }
}
