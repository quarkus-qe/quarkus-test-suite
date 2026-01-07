package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.nocache;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.runtime.DefaultTokenIntrospectionUserInfoCache;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@PermitAll
@Path("/no-cache")
public class NoCacheFlow {
    @Inject
    SecurityIdentity identity;

    @Inject
    DefaultTokenIntrospectionUserInfoCache tokenCache;

    @GET
    @Authenticated
    public String access() {
        return identity.getPrincipal().getName() + ", cache size: " + tokenCache.getCacheSize();
    }
}
