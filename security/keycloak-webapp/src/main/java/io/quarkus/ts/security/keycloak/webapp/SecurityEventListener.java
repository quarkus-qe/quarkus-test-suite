package io.quarkus.ts.security.keycloak.webapp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.OidcProviderClient;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.oidc.SecurityEvent;

@ApplicationScoped
public class SecurityEventListener {

    public void event(@ObservesAsync SecurityEvent event) {
        System.out.println("SecurityEventListener.event: " + event.getEventType());
        if (SecurityEvent.Type.OIDC_LOGOUT_RP_INITIATED == event.getEventType()) {
            OidcProviderClient oidcProvider = event.getSecurityIdentity().getAttribute(OidcProviderClient.class.getName());
            String accessToken = event.getSecurityIdentity().getCredential(AccessTokenCredential.class).getToken();
            System.out.println("SecurityEventListener.event: accessToken = " + accessToken);
            String refreshToken = event.getSecurityIdentity().getCredential(RefreshToken.class).getToken();
            oidcProvider.revokeAccessToken(accessToken).await().indefinitely();
            oidcProvider.revokeRefreshToken(refreshToken).await().indefinitely();
        }
    }

}
