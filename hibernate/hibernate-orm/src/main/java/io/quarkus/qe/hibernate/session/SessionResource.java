package io.quarkus.qe.hibernate.session;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hibernate.Session;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.qe.hibernate.interceptor.SessionEventInterceptor;
import io.quarkus.qe.hibernate.items.MyEntity;
import io.quarkus.qe.hibernate.items.MyEntityId;

@Path("/session")
public class SessionResource {

    @Named("named")
    @Inject
    Session session;

    @PersistenceUnitExtension("named")
    @Inject
    SessionEventInterceptor interceptor;

    @Transactional
    @Path("/persist/{id}")
    @POST
    public void persist(@PathParam("id") int id, String data) {
        var entity = new MyEntity();
        entity.setAnId(new MyEntityId(id));
        entity.setData(data);
        session.persist(entity);
    }

    @Path("/find/{id}")
    @GET
    public String find(@PathParam("id") int id) {
        return session.find(MyEntity.class, new MyEntityId(id)).getData();
    }

    @Transactional
    @Path("/remove/{id}")
    @DELETE
    public boolean remove(@PathParam("id") int id) {
        MyEntity entity = session.getReference(MyEntity.class, new MyEntityId(id));
        if (entity == null) {
            throw new WebApplicationException("MyEntity with id of " + id + " does not exist.", 404);
        }
        session.remove(entity);
        return true;
    }

    @Path("/get-reference/{id}")
    @GET
    public Response getReference(@PathParam("id") int id) {
        final String data;
        try {
            data = session.getReference(MyEntity.class, new MyEntityId(id)).getData();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return Response.status(404).build();
        }
        return Response.ok(data).build();
    }

    @Transactional
    @Path("/merge/{id}")
    @POST
    public void merge(@PathParam("id") int id, String data) {
        var entity = new MyEntity();
        entity.setAnId(new MyEntityId(id));
        entity.setData(data);
        session.merge(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/merge/intercepted-data")
    public SessionEventInterceptor.MergeData getInterceptedMergeData() {
        return interceptor.getMergeData();
    }
}
