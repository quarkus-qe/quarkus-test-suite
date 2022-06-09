package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import org.junit.jupiter.api.BeforeEach;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public abstract class BaseOidcIT {
    static final String USER = "test-user";

    static final int KEYCLOAK_PORT = 8080;
    static final String REALM_DEFAULT = "test-realm";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @Container(image = "${keycloak.image}", expectedLog = "Http management interface listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT)
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    private AuthzClient authzClient;

    @BeforeEach
    public void setup() {
        authzClient = keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT);
    }

    protected String createToken() {
        return authzClient.obtainAccessToken(USER, USER).getToken();
    }
}
