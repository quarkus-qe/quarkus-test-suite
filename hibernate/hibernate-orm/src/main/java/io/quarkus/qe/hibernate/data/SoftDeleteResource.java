package io.quarkus.qe.hibernate.data;

import jakarta.inject.Named;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.hibernate.LockMode;
import org.hibernate.StatelessSession;

@Path("/soft-delete/{id}")
public class SoftDeleteResource {

    private final StatelessSession statelessSession;

    public SoftDeleteResource(@Named("named-2") StatelessSession statelessSession) {
        this.statelessSession = statelessSession;
    }

    @Transactional
    @POST
    public void create(@PathParam("id") long id) {
        var entity = new SoftDeleteEntity();
        entity.setId(id);
        statelessSession.insert(entity);
    }

    @GET
    public Response getById(@PathParam("id") long id) {
        try {
            // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 StatelessSession.get() NPE with 2nd level cache
            // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
            var entity = statelessSession.get(SoftDeleteEntity.class, id, LockMode.NONE);
            return Response.ok(entity.getId()).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Transactional
    @DELETE
    public void delete(@PathParam("id") long id) {
        // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 StatelessSession.get() NPE with 2nd level cache
        // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
        statelessSession.delete(statelessSession.get(SoftDeleteEntity.class, id, LockMode.NONE));
    }

    @GET
    @Path("/get-timestamp")
    public String getTimestamp(@PathParam("id") long id) {
        String timestamp = statelessSession
                .createNativeQuery("SELECT deleted FROM soft_delete WHERE id = :id", String.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        return timestamp == null ? "" : timestamp;
    }

    @Transactional
    @Path("/home-address")
    @PUT
    public void setHomeAddress(@PathParam("id") long id, @QueryParam("street") String street, @QueryParam("city") String city,
            @QueryParam("state") String state, @QueryParam("zip") String zip) {
        // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 StatelessSession.get() NPE with 2nd level cache
        // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
        var entity = statelessSession.get(SoftDeleteEntity.class, id, LockMode.NONE);
        entity.setHomeAddress(new Address(street, city, state, zip));
        statelessSession.update(entity);
    }

    @Transactional
    @Path("/work-address")
    @PUT
    public void setWorkAddress(@PathParam("id") long id, @QueryParam("street") String street, @QueryParam("city") String city,
            @QueryParam("state") String state, @QueryParam("zip") String zip) {
        // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 StatelessSession.get() NPE with 2nd level cache
        // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
        var entity = statelessSession.get(SoftDeleteEntity.class, id, LockMode.NONE);
        entity.setWorkAddress(new Address(street, city, state, zip));
        statelessSession.update(entity);
    }

    @GET
    @Path("/get-home-address-street")
    public String getHomeAddress(@PathParam("id") long id) {
        String timestamp = statelessSession
                .createNativeQuery("SELECT homeAddress_street FROM soft_delete WHERE id = :id", String.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        return timestamp == null ? "" : timestamp;
    }

    @GET
    @Path("/get-work-address-street")
    public String getWorkAddress(@PathParam("id") long id) {
        String timestamp = statelessSession
                .createNativeQuery("SELECT workAddress_street FROM soft_delete WHERE id = :id", String.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        return timestamp == null ? "" : timestamp;
    }
}
