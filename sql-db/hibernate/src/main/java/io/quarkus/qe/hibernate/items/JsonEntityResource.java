package io.quarkus.qe.hibernate.items;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/json-entity")
public class JsonEntityResource {

    @PersistenceUnit("named")
    @Inject
    EntityManager entityManager;

    @POST
    @Transactional
    @Path("/create/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String create(@PathParam("name") String name, String jsonData) {
        JsonEntity entity = new JsonEntity(name, jsonData);
        entityManager.persist(entity);
        return String.valueOf(entity.getId());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@PathParam("id") Long id) {
        JsonEntity entity = entityManager.find(JsonEntity.class, id);
        return entity.getJsonData();
    }
}
