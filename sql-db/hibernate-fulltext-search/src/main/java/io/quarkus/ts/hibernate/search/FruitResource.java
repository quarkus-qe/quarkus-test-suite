package io.quarkus.ts.hibernate.search;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.logging.Logger;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/{tenant}/fruits")
public class FruitResource {

    private static final Logger LOG = Logger.getLogger(FruitResource.class.getName());

    @Inject
    EntityManager entityManager;
    @Inject
    SearchSession searchSession;

    @GET
    @Path("/")
    @Transactional
    @Blocking
    public Fruit[] getAll() {
        return entityManager.createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList().toArray(new Fruit[0]);
    }

    @GET
    @Path("/{id}")
    @Transactional
    @Blocking
    public Fruit findById(int id) {
        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Path("/")
    @Transactional
    @Blocking
    public Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        LOG.debugv("Create {0}", fruit.getName());
        entityManager.persist(fruit);
        return Response.ok(fruit).status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Blocking
    public Fruit update(@PathParam("id") int id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.setName(fruit.getName());

        LOG.debugv("Update #{0} {1}", fruit.getId(), fruit.getName());

        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Blocking
    public Response delete(@PathParam("id") int id) {
        Fruit fruit = entityManager.getReference(Fruit.class, id);
        if (fruit == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        LOG.debugv("Delete #{0} {1}", fruit.getId(), fruit.getName());
        entityManager.remove(fruit);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/search")
    @Transactional
    @Blocking
    public Response search(@QueryParam("terms") String terms) {
        List<Fruit> list = searchSession.search(Fruit.class)
                .where(f -> f.simpleQueryString().field("name").matching(terms))
                .sort(f -> f.field("fruitName_sort"))
                .fetchAllHits();
        return Response.status(Response.Status.OK).entity(list).build();
    }
}
