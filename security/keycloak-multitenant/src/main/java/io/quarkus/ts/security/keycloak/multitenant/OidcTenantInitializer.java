package io.quarkus.ts.security.keycloak.multitenant;

import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.oidc.Oidc;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.OidcConfig;

public class OidcTenantInitializer {

    void observe(@Observes Oidc oidc, OidcConfig oidcConfig,
            @ConfigProperty(name = "quarkus.oidc.auth-server-url") String authServerUrl) {
        oidc.create(createDefaultTenant(oidcConfig));
        oidc.create(createNamedTenant(authServerUrl));
    }

    private OidcTenantConfig createDefaultTenant(OidcConfig oidcConfig) {
        // this enhances 'application.properties' configuration with a tenant path
        return OidcTenantConfig.builder(OidcConfig.getDefaultTenant(oidcConfig))
                .tenantPaths("/extra-default-tenant-path")
                .build();
    }

    private OidcTenantConfig createNamedTenant(String authServerUrl) {
        return OidcTenantConfig.authServerUrl(authServerUrl)
                .tenantId("named")
                .tenantPaths("/user-info/named-tenant-random")
                .userInfoPath("%s/protocol/openid-connect/userinfo".formatted(authServerUrl))
                .build();
    }

}
