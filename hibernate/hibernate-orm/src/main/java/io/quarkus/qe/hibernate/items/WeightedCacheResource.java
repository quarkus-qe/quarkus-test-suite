package io.quarkus.qe.hibernate.items;

import java.util.OptionalLong;

import jakarta.inject.Inject;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.SessionFactory;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cache.spi.CacheImplementor;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/weighted-cache")
public class WeightedCacheResource {

    public record RegionStats(Long maximumWeight, Long maximumSize, boolean hasWeigher, Long weightedSize, long estimatedSize) {
    }

    @Inject
    @PersistenceUnit("named")
    EntityManagerFactory entityManagerFactory;

    @Inject
    @PersistenceUnit("named")
    EntityManager entityManager;

    @Inject
    @PersistenceUnit("named")
    Cache jpaCache;

    @POST
    @Path("/weighted")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Long persistWeighted(@QueryParam("size") int size) {
        WeightedCacheEntity entity = new WeightedCacheEntity();
        entity.setData("x".repeat(size));
        entityManager.persist(entity);
        return entity.getId();
    }

    @POST
    @Path("/idle-weighted")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Long persistIdleWeighted(@QueryParam("size") int size) {
        IdleWeightedCacheEntity entity = new IdleWeightedCacheEntity();
        entity.setData("x".repeat(size));
        entityManager.persist(entity);
        return entity.getId();
    }

    @POST
    @Path("/counted")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Long persistCounted(@QueryParam("size") int size) {
        CountedCacheEntity entity = new CountedCacheEntity();
        entity.setData("x".repeat(size));
        entityManager.persist(entity);
        return entity.getId();
    }

    @POST
    @Path("/default-counted")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Long persistDefaultCounted(@QueryParam("size") int size) {
        DefaultCountedCacheEntity entity = new DefaultCountedCacheEntity();
        entity.setData("x".repeat(size));
        entityManager.persist(entity);
        return entity.getId();
    }

    @GET
    @Path("/weighted/{id}/cached")
    public boolean isWeightedCached(@PathParam("id") long id) {
        return jpaCache.contains(WeightedCacheEntity.class, id);
    }

    @GET
    @Path("/idle-weighted/{id}/cached")
    public boolean isIdleWeightedCached(@PathParam("id") long id) {
        return jpaCache.contains(IdleWeightedCacheEntity.class, id);
    }

    @GET
    @Path("/region/{region}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegionStats stats(@PathParam("region") String region) {
        var sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        var regionFactory = (JCacheRegionFactory) ((CacheImplementor) sessionFactory.getCache()).getRegionFactory();
        javax.cache.Cache<?, ?> regionCache = regionFactory.getCacheManager().getCache(region);
        CaffeineConfiguration<?, ?> config = regionCache.getConfiguration(CaffeineConfiguration.class);
        com.github.benmanes.caffeine.cache.Cache<?, ?> cache = regionCache
                .unwrap(com.github.benmanes.caffeine.cache.Cache.class);
        cache.cleanUp();
        return new RegionStats(
                toNullable(config.getMaximumWeight()),
                toNullable(config.getMaximumSize()),
                config.getWeigherFactory().isPresent(),
                toNullable(cache.policy().eviction().orElseThrow().weightedSize()),
                cache.estimatedSize());
    }

    private static Long toNullable(OptionalLong value) {
        return value.isPresent() ? value.getAsLong() : null;
    }
}
