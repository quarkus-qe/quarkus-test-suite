package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import org.jboss.logging.Logger;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

public abstract class BaseOidcIT {
    private static final Logger LOG = Logger.getLogger(BaseOidcIT.class);
    static final String USER = "test-user";

    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    protected String createToken() {
        // we retry and create AuthzClient as we experienced following exception in the past: 'Caused by:
        // org.apache.http.NoHttpResponseException: keycloak-ts-juwpkvyduk.apps.ocp4-15.dynamic.quarkus:80 failed to respond'
        return createToken(1);
    }

    private static String createToken(int attemptCount) {
        try {
            return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT).obtainAccessToken(USER, USER)
                    .getToken();
        } catch (RuntimeException e) {
            LOG.error("Attempt #%d to create token failed with exception:".formatted(attemptCount), e);
            if (e.getCause() instanceof org.apache.http.NoHttpResponseException && attemptCount < 3) {
                LOG.info("Retrying to create token.");
                return createToken(attemptCount + 1);
            }
            throw e;
        }
    }
}
