package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.quarkus.oidc.runtime.DefaultTokenIntrospectionUserInfoCache;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@PermitAll
@Path("/code-flow")
public class LogoutFlow {
    @Inject
    SecurityIdentity identity;

    @Inject
    DefaultTokenIntrospectionUserInfoCache tokenCache;

    @GET
    @Authenticated
    public String access() {
        return identity.getPrincipal().getName() + ", cache size: " + tokenCache.getCacheSize();
    }

    @GET
    @Path("/authenticated")
    @Authenticated
    public String authenticatedResourceExample() {
        return "Authenticated resource!";
    }
}
