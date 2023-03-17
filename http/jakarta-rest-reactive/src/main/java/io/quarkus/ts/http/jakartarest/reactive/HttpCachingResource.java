package io.quarkus.ts.http.jakartarest.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.Cache;
import org.jboss.resteasy.reactive.NoCache;

@Cache
@Path("/http-caching")
public class HttpCachingResource {
    public static final String RESPONSE = "hello";

    @GET
    @Path("/no-attributes")
    public String noAttributes() {
        return RESPONSE;
    }

    @GET
    @Path("/all-attributes")
    @Cache(maxAge = 1, sMaxAge = 1, noStore = true, noTransform = true, mustRevalidate = true, proxyRevalidate = true, isPrivate = true, noCache = true)
    public String allAttributes() {
        return RESPONSE;
    }

    @GET
    @Path("/nocache-unqualified")
    @NoCache
    public String noCacheUnqualified() {
        return RESPONSE;
    }

    @GET
    @Path("/nocache-qualified")
    @NoCache(fields = { "field1" })
    public String noCacheQualified() {
        return RESPONSE;
    }
}
