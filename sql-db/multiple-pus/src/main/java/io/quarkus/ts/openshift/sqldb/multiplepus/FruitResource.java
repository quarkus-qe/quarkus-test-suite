package io.quarkus.ts.openshift.sqldb.multiplepus;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.panache.common.Sort;
import io.quarkus.ts.openshift.sqldb.multiplepus.model.fruit.Fruit;

@Path("fruit")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FruitResource {

    @Inject
    @PersistenceUnit("fruits")
    EntityManager entityManager;

    @GET
    public List<Fruit> getAll() {
        return Fruit.listAll(Sort.by("name"));
    }

    @GET
    @Path("/{id}")
    public Fruit get(@PathParam("id") Long id) {
        final Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("fruit '" + id + "' not found");
        }
        return fruit;
    }

    @POST
    @Transactional
    public Response create(@Valid Fruit fruit) {
        if (fruit.id != null) {
            throw new ClientErrorException("unexpected ID in request", ValidationExceptionMapper.UNPROCESSABLE_ENTITY);
        }

        fruit.persist();
        return Response.ok(fruit).status(CREATED.getStatusCode()).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Fruit update(@PathParam("id") Long id, @Valid Fruit newFruit) {
        final Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("fruit '" + id + "' not found");
        }

        fruit.name = newFruit.name;
        return fruit;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        final Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("fruit '" + id + "' not found");
        }
        fruit.delete();
        return Response.status(NO_CONTENT.getStatusCode()).build();
    }

}
