package io.quarkus.ts.sb.reactive;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.smallrye.mutiny.Uni;

@Path("/todo")
public class TodoResource {

    @GET
    @Path("/{id}")
    public Uni<Todo> get(@PathParam("id") Long id) {
        return Todo.findById(id);
    }

}
