package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.rar;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantFeature;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.smallrye.mutiny.Uni;

@TenantFeature("rar")
@OidcEndpoint(value = OidcEndpoint.Type.TOKEN)
@ApplicationScoped
public class RarTokenResponseFilter implements OidcResponseFilter {

    private volatile String authorizationDetails;

    @Override
    public Uni<Void> filter(OidcResponseFilterContext responseContext) {
        authorizationDetails = responseContext.responseBody()
                .toJsonObject()
                .getString("authorization_details");
        return Uni.createFrom().voidItem();
    }

    public String getAuthorizationDetails() {
        return authorizationDetails;
    }
}
