package io.quarkus.ts.sb.postgresql;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/todo")
public class TodoResource {

    @GET
    @Path("/")
    public List<Todo> getAll() {
        return Todo.listAll();
    }

    @GET
    @Path("/{id}")
    public Todo get(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        return entity;
    }

    @POST
    @Path("/")
    @Transactional
    public Response create(Todo item) {
        item.persist();
        return Response.status(Status.CREATED).entity(item).build();
    }

    @PUT
    @Path("/{id}/complete")
    @Transactional
    public Response update(@PathParam("id") Long id) {
        Todo.update("completed = 'true' where id = ?", id);
        return Response.ok(Todo.findById(id)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Todo.deleteById(id);
        return Response.noContent().build();
    }

}
