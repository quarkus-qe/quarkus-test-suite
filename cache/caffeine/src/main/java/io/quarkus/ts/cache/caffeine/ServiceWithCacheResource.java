package io.quarkus.ts.cache.caffeine;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/services")
public class ServiceWithCacheResource {

    public static final String APPLICATION_SCOPE_SERVICE_PATH = "application-scope";
    public static final String REQUEST_SCOPE_SERVICE_PATH = "request-scope";

    @Inject
    ApplicationScopeService applicationScopeService;

    @Inject
    RequestScopeService requestScopeService;

    @GET
    @Path("/{service}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValueFromService(@PathParam("service") String service) {
        return lookupServiceByPathParam(service).getValue();
    }

    @POST
    @Path("/{service}/invalidate-cache")
    public void invalidateCacheFromService(@PathParam("service") String service) {
        lookupServiceByPathParam(service).invalidate();
    }

    @POST
    @Path("/{service}/invalidate-cache-all")
    public void invalidateCacheAllFromService(@PathParam("service") String service) {
        lookupServiceByPathParam(service).invalidateAll();
    }

    @GET
    @Path("/{service}/using-prefix/{prefix}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValueUsingPrefixFromService(@PathParam("service") String service,
            @PathParam("prefix") String prefix) {
        return lookupServiceByPathParam(service).getValueWithPrefix(prefix);
    }

    @POST
    @Path("/{service}/using-prefix/{prefix}/invalidate-cache")
    public void invalidateCacheUsingPrefixFromService(@PathParam("service") String service,
            @PathParam("prefix") String prefix) {
        lookupServiceByPathParam(service).invalidateWithPrefix(prefix);
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
