package io.quarkus.qe.hibernate.items;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("entity-creation")
public class MyEntityResource {

    @Inject
    @PersistenceUnit("named")
    EntityManager entityManager;

    @POST
    @Path("create-and-update")
    @Transactional
    public Response createAndUpdateMyEntity(@QueryParam("initData") String initData,
            @QueryParam("updateData") String updateData, @QueryParam("id") long id) {
        // Creating entity which will be updated in same transaction.
        MyEntity entity = new MyEntity();
        entity.setAnId(new MyEntityId(id));
        entity.setData(initData);
        entityManager.persist(entity);

        MyEntityId newIdInstance = new MyEntityId(id);
        entity.setAnId(newIdInstance);
        entity.setData(updateData);

        return Response.ok("Entity created and updated!").status(201).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public MyEntity findEntity(@PathParam("id") long id) {
        return entityManager.find(MyEntity.class, new MyEntityId(id));
    }
}
