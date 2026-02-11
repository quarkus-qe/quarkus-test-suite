package io.quarkus.ts.cache.multiprovider.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.cache.multiprovider.services.ApplicationScopeService;
import io.quarkus.ts.cache.multiprovider.services.BaseServiceWithCache;
import io.quarkus.ts.cache.multiprovider.services.RequestScopeService;

@Path("/services")
public class CacheServicesWithCacheResource {

    public static final String APPLICATION_SCOPE_SERVICE_PATH = "application-scope";
    public static final String REQUEST_SCOPE_SERVICE_PATH = "request-scope";

    @Inject
    ApplicationScopeService applicationScopeService;

    @Inject
    RequestScopeService requestScopeService;

    @GET
    @Path("/{cache-name}/{service}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValueFromService(@PathParam("service") String service, @PathParam("cache-name") String cacheName) {
        var serviceByPathParam = lookupServiceByPathParam(service);
        return "redis".equals(cacheName) ? serviceByPathParam.redisGetValue() : serviceByPathParam.caffeineGetValue();
    }

    @POST
    @Path("/{cache-name}/{service}/invalidate-cache")
    public void invalidateCacheFromService(@PathParam("service") String service, @PathParam("cache-name") String cacheName) {
        var serviceByPathParam = lookupServiceByPathParam(service);
        if ("redis".equals(cacheName)) {
            serviceByPathParam.redisInvalidate();
        } else {
            serviceByPathParam.caffeineInvalidate();
        }
    }

    @POST
    @Path("/{cache-name}/{service}/invalidate-cache-all")
    public void invalidateCacheAllFromService(@PathParam("service") String service, @PathParam("cache-name") String cacheName) {
        var serviceByPathParam = lookupServiceByPathParam(service);
        if ("redis".equals(cacheName)) {
            serviceByPathParam.redisInvalidateAll();
        } else {
            serviceByPathParam.caffeineInvalidateAll();
        }
    }

    @GET
    @Path("/{cache-name}/{service}/using-prefix/{prefix}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValueUsingPrefixFromService(@PathParam("service") String service,
            @PathParam("prefix") String prefix, @PathParam("cache-name") String cacheName) {
        var serviceByPathParam = lookupServiceByPathParam(service);
        return "redis".equals(cacheName) ? serviceByPathParam.redisGetValueWithPrefix(prefix)
                : serviceByPathParam.caffeineGetValueWithPrefix(prefix);
    }

    @POST
    @Path("/{cache-name}/{service}/using-prefix/{prefix}/invalidate-cache")
    public void invalidateCacheUsingPrefixFromService(@PathParam("service") String service,
            @PathParam("prefix") String prefix, @PathParam("cache-name") String cacheName) {
        var serviceByPathParam = lookupServiceByPathParam(service);
        if ("redis".equals(cacheName)) {
            serviceByPathParam.redisInvalidateWithPrefix(prefix);
        } else {
            serviceByPathParam.caffeineInvalidateWithPrefix(prefix);
        }
    }

    private BaseServiceWithCache lookupServiceByPathParam(String service) {
        if (APPLICATION_SCOPE_SERVICE_PATH.equals(service)) {
            return applicationScopeService;
        } else if (REQUEST_SCOPE_SERVICE_PATH.equals(service)) {
            return requestScopeService;
        }

        throw new IllegalArgumentException("Service " + service + " is not recognised");
    }
}
