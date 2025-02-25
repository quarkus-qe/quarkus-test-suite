package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/meta")
public class MetaResource {
    @GET
    @Path("/class/{name}")
    public Response getClass(@PathParam("name") String className) {
        try {
            Class<?> existingclass = Class.forName(className);
            return Response.ok(existingclass.getCanonicalName()).build();
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.NO_CONTENT.getStatusCode(),
                    "There is no such class: " + className).build();
        }
    }
}
