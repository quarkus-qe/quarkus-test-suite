package io.quarkus.ts.sqldb.multiplepus;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.panache.common.Sort;
import io.quarkus.ts.sqldb.multiplepus.model.vegetable.Vegetable;

@Path("vegetable")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VegetableResource {

    @Inject
    @PersistenceUnit("vegetables")
    EntityManager entityManager;

    @GET
    public List<Vegetable> getAll() {
        return Vegetable.listAll(Sort.by("name"));
    }

    @GET
    @Path("/{id}")
    public Vegetable get(@PathParam("id") Long id) {
        final Vegetable vegetable = Vegetable.findById(id);
        if (vegetable == null) {
            throw new NotFoundException("vegetable '" + id + "' not found");
        }
        return vegetable;
    }

    @POST
    @Transactional
    public Response create(@Valid Vegetable vegetable) {
        if (vegetable.id != null) {
            throw new ClientErrorException("unexpected ID in request", ValidationExceptionMapper.UNPROCESSABLE_ENTITY);
        }

        vegetable.persist();
        return Response.ok(vegetable).status(CREATED.getStatusCode()).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Vegetable update(@PathParam("id") Long id, @Valid Vegetable newVegetable) {
        final Vegetable vegetable = Vegetable.findById(id);
        if (vegetable == null) {
            throw new NotFoundException("vegetable '" + id + "' not found");
        }

        vegetable.name = newVegetable.name;
        return vegetable;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        final Vegetable vegetable = Vegetable.findById(id);
        if (vegetable == null) {
            throw new NotFoundException("vegetable '" + id + "' not found");
        }
        vegetable.delete();
        return Response.status(NO_CONTENT.getStatusCode()).build();
    }

}
