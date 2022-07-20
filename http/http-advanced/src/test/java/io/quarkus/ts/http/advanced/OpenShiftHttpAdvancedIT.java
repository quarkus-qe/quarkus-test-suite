package io.quarkus.ts.http.advanced;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftHttpAdvancedIT extends BaseHttpAdvancedIT {

    private static final int KEYCLOAK_PORT = 8080;

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @Container(image = "${keycloak.image}", expectedLog = "Admin console listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT)
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService().withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected Protocol getProtocol() {
        // HTTPs is not supported in OpenShift yet. The same happens in OpenShift TS.
        return Protocol.HTTP;
    }

}
