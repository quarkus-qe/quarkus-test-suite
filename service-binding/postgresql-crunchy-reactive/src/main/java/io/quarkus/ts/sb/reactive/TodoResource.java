package io.quarkus.ts.sb.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.smallrye.mutiny.Uni;

@Path("/todo")
public class TodoResource {

    @GET
    @Path("/{id}")
    public Uni<Todo> get(@PathParam("id") Long id) {
        return Todo.findById(id);
    }

}
