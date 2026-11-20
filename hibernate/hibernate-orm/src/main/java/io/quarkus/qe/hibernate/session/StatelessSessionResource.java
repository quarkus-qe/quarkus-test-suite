package io.quarkus.qe.hibernate.session;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Cache;
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
import org.hibernate.jpa.AvailableHints;

import io.quarkus.qe.hibernate.items.MyCachedEntity;
import io.quarkus.qe.hibernate.items.MyEntityId;

@Path("/stateless-session")
public class StatelessSessionResource {

    @Named("named")
    @Inject
    StatelessSession statelessSession;

    @Named("named")
    @Inject
    Cache cache;

    @Transactional
    @Path("/insert/{id}")
    @POST
    public void insert(@PathParam("id") int id, String data) {
        var entity = new MyCachedEntity();
        entity.setId(new MyEntityId(id));
        entity.setData(data);
        statelessSession.insert(entity);
    }

    @Transactional
    @Path("/update/{id}")
    @PUT
    public void update(@PathParam("id") int id, String data) {
        var entity = new MyCachedEntity();
        entity.setId(new MyEntityId(id));
        entity.setData(data);
        statelessSession.update(entity);
    }

    @GET
    @Path("/get/{id}")
    public Response getDataById(@PathParam("id") int id) {
        try {
            // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 bug where
            // StatelessSession.get(Class, Object) passes null LockMode to the 2nd level cache,
            // causing NPE in CacheLoadHelper.loadFromSecondLevelCache()
            // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
            var entity = statelessSession.get(MyCachedEntity.class, new MyEntityId(id), LockMode.NONE);
            return Response.ok(entity.getData()).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Transactional
    @DELETE
    @Path("/delete/{id}")
    public void delete(@PathParam("id") int id) {
        // TODO Quarkus 4: explicit LockMode.NONE workaround for Hibernate ORM 8 StatelessSession.get() NPE with 2nd level cache
        // Fix: https://github.com/hibernate/hibernate-orm/pull/13701
        var entity = statelessSession.get(MyCachedEntity.class, new MyEntityId(id), LockMode.NONE);
        statelessSession.delete(entity);
    }

    @Transactional
    @PUT
    @Path("/update-using-connection-directly")
    public void updateUsingConnectionDirectly() {
        statelessSession.doWork(connection -> {
            connection
                    .prepareStatement("UPDATE my_cached_entity SET data = CONCAT(data, data)")
                    .executeUpdate();
        });
    }

    @GET
    @Path("/cache/contains/{id}")
    public boolean isCached(@PathParam("id") int id) {
        return cache.contains(MyCachedEntity.class, new MyEntityId(id));
    }

    @DELETE
    @Path("/cache/evict-all")
    public void evictAll() {
        cache.evictAll();
    }

    @GET
    @Path("/get-content-using-query-with-cacheable-hint")
    public Response getUsingQueryWithCacheableHint(@QueryParam("id") long id, @QueryParam("cacheable") boolean cacheable) {
        var entity = statelessSession
                .createQuery("from MyCachedEntity e where e.id = :id", MyCachedEntity.class)
                .setParameter("id", new MyEntityId(id))
                .setHint(AvailableHints.HINT_CACHEABLE, cacheable)
                .getSingleResultOrNull();
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entity.getData()).build();
    }

    @GET
    @Path("/get-content-using-query-with-cacheable-option")
    public Response getUsingQueryWithCacheableOption(@QueryParam("id") long id, @QueryParam("cacheable") boolean cacheable) {
        var entity = statelessSession
                .createQuery("from MyCachedEntity e where e.id = :id", MyCachedEntity.class)
                .setParameter("id", new MyEntityId(id))
                .setCacheable(cacheable)
                .getSingleResultOrNull();
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entity.getData()).build();
    }
}
