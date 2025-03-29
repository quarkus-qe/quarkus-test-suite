package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
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

    @GET
    @Path("/headers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getHeaders(HttpHeaders info) {
        List<String> headers = new ArrayList<>();
        info.getRequestHeaders().forEach((key, value) -> headers.add(key + ": " + value));

        return headers;
    }

}
