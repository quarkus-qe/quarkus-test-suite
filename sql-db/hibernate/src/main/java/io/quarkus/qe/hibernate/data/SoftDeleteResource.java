package io.quarkus.qe.hibernate.data;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

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
        var entity = statelessSession.get(SoftDeleteEntity.class, id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entity.getId()).build();
    }

    @Transactional
    @DELETE
    public void delete(@PathParam("id") long id) {
        statelessSession.delete(statelessSession.get(SoftDeleteEntity.class, id));
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
        var entity = statelessSession.get(SoftDeleteEntity.class, id);
        entity.setHomeAddress(new Address(street, city, state, zip));
        statelessSession.update(entity);
    }

    @Transactional
    @Path("/work-address")
    @PUT
    public void setWorkAddress(@PathParam("id") long id, @QueryParam("street") String street, @QueryParam("city") String city,
            @QueryParam("state") String state, @QueryParam("zip") String zip) {
        var entity = statelessSession.get(SoftDeleteEntity.class, id);
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
