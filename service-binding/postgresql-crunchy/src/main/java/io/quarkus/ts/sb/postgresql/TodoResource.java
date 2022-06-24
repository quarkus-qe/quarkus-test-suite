package io.quarkus.ts.sb.postgresql;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

    @GET
    @Path("/{id}/complete")
    @Transactional
    public Response complete(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        entity.id = id;
        entity.completed = true;
        return Response.ok(entity).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        entity.delete();
        return Response.noContent().build();
    }

}
