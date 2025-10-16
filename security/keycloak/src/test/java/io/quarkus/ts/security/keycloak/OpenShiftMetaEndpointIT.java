package io.quarkus.ts.security.keycloak;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.URILike;

@OpenShiftScenario
public class OpenShiftMetaEndpointIT extends AbstractMetaEndpointIT {

    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @Override
    URILike getAppURL(RestService app, Protocol protocol) {
        // For some reason, when running on OpenStack, the URI contains port number,
        // which is not part of the real route. Here and in other methods, we remove it by using -1 value
        // see https://github.com/openjdk/jdk/blob/jdk-21-ga/src/java.base/share/classes/java/net/URI.java#L2065-L2067
        return app.getURI(protocol).withPort(-1);
    }
}
