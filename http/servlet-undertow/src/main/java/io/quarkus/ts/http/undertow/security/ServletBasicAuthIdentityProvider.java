package io.quarkus.ts.http.undertow.security;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ServletBasicAuthIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest,
            AuthenticationRequestContext authenticationRequestContext) {
        return authenticationRequestContext.runBlocking(() -> withIdentity(usernamePasswordAuthenticationRequest));
    }

    @ActivateRequestContext
    SecurityIdentity withIdentity(UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest) {
        final SecurityIdentity identity;
        var username = usernamePasswordAuthenticationRequest.getUsername();
        var isPablo = "Pablo".equals(username);
        if (isPablo || "Rocky".equals(username)) {

            if (!Arc.container().requestContext().isActive()) {
                throw new IllegalStateException("The request scope should be active");
            }

            final Set<String> roles;
            if (isPablo) {
                roles = Set.of("granados");
            } else {
                // unauthorized
                roles = Set.of();
            }
            identity = QuarkusSecurityIdentity
                    .builder()
                    .setPrincipal(new QuarkusPrincipal(username))
                    .addRoles(roles)
                    .build();
        } else {
            // unauthenticated
            identity = null;
        }
        return identity;
    }
}
