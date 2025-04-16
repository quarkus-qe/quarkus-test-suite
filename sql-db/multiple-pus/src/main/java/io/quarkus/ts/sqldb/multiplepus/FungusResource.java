package io.quarkus.ts.sqldb.multiplepus;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.ts.sqldb.multiplepus.model.fungus.Fungus;

@Path("fungus")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FungusResource {

    @GET
    public long countAll() {
        return Fungus.count();
    }

    @POST
    @Transactional
    public Response create(@Valid Fungus fungus) {
        if (fungus.id != null) {
            throw new ClientErrorException("unexpected ID in request", ValidationExceptionMapper.UNPROCESSABLE_ENTITY);
        }

        fungus.persist();
        return Response.ok(fungus).status(CREATED.getStatusCode()).build();
    }

    @GET
    @Path("/{id}")
    public Fungus get(@PathParam("id") Long id) {
        final Fungus fungus = Fungus.findById(id);
        if (fungus == null) {
            throw new NotFoundException("fungus '" + id + "' not found");
        }
        return fungus;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        final Fungus fungus = Fungus.findById(id);
        if (fungus == null) {
            throw new NotFoundException("Fungus '" + id + "' not found");
        }
        fungus.delete();
        return Response.status(NO_CONTENT.getStatusCode()).build();
    }

}
