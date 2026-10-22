package io.quarkus.ts.hibernate.reactive.http;

import java.util.OptionalLong;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.reactive.mutiny.Mutiny;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;

import io.quarkus.arc.Arc;
import io.quarkus.ts.hibernate.reactive.database.CachedDocument;
import io.smallrye.mutiny.Uni;

@Path("/cached-document")
public class CachedDocumentResource {

    public record RegionStats(Long maximumWeight, Long maximumSize, boolean hasWeigher, Long weightedSize, long estimatedSize) {
    }

    Mutiny.SessionFactory factory;

    public CachedDocumentResource() {
        // Not injecting Mutiny.SessionFactory directly because this module has more than one un-named datasources.
        factory = Arc.container().instance(Mutiny.SessionFactory.class).get();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Long> persist(@QueryParam("size") int size) {
        CachedDocument document = new CachedDocument();
        document.setData("x".repeat(size));
        return factory.withTransaction(session -> session.persist(document))
                .map(ignored -> document.getId());
    }

    @GET
    @Path("/{id}/cached")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isCached(@PathParam("id") long id) {
        return factory.getCache().contains(CachedDocument.class, id);
    }

    @GET
    @Path("/region/{region}")
    @Produces(MediaType.APPLICATION_JSON)
    public RegionStats stats(@PathParam("region") String region) {
        var regionFactory = (JCacheRegionFactory) ((CacheImplementor) factory.getCache()).getRegionFactory();
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
