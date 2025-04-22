package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import org.jboss.logging.Logger;

import io.quarkus.test.bootstrap.KeycloakService;

final class TokenUtils {

    private static final Logger LOG = Logger.getLogger(TokenUtils.class);
    static final String USER = "test-user";

    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    private TokenUtils() {
    }

    static String createToken(KeycloakService keycloak) {
        // we retry and create AuthzClient as we experienced following exception in the past: 'Caused by:
        // org.apache.http.NoHttpResponseException: keycloak-ts-juwpkvyduk.apps.ocp4-15.dynamic.quarkus:80 failed to respond'
        return createToken(1, keycloak);
    }

    private static String createToken(int attemptCount, KeycloakService keycloak) {
        try {
            return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT).obtainAccessToken(USER, USER)
                    .getToken();
        } catch (RuntimeException e) {
            LOG.error("Attempt #%d to create token failed with exception:".formatted(attemptCount), e);
            if (e.getCause() instanceof org.apache.http.NoHttpResponseException && attemptCount < 3) {
                LOG.info("Retrying to create token.");
                return createToken(attemptCount + 1, keycloak);
            }
            throw e;
        }
    }

}
