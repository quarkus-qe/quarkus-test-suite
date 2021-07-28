package io.quarkus.ts.many.extensions;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/resource/steep")
public class ResourceSteep {
    @Inject
    ServiceSteep service;

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Hello process(@PathParam("name") String name) {
        return new Hello(service.process(name));
    }
}
