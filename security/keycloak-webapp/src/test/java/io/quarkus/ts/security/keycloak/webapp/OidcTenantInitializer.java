package io.quarkus.ts.security.keycloak.webapp;

import static io.quarkus.oidc.runtime.OidcTenantConfig.ApplicationType.WEB_APP;
import static io.quarkus.oidc.runtime.OidcTenantConfig.Roles.Source.accesstoken;

import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.oidc.Oidc;
import io.quarkus.oidc.OidcTenantConfig;

public class OidcTenantInitializer {
    void observe(@Observes Oidc oidc, @ConfigProperty(name = "quarkus.oidc.auth-server-url") String authServerUrl) {
        OidcTenantConfig defaultTenant = OidcTenantConfig.authServerUrl(authServerUrl)
                .clientId("test-application-client")
                .applicationType(WEB_APP)
                .credentials("test-application-client-secret")
                .roles().source(accesstoken).end()
                .token().lifespanGrace(5).end()
                .logout().path("/logout").end()
                .build();
        oidc.create(defaultTenant);
    }
}
