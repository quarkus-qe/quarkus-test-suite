package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@QuarkusScenario
public class SpiffeClientAuthIT extends AbstractSpiffeClientAuthIT {

    private static final Logger LOG = Logger.getLogger(SpiffeClientAuthIT.class);

    @KeycloakContainer(runKeycloakInProdMode = true, command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=client-auth-federated,spiffe" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @BeforeAll
    public static void setup() {
        int appPort = app.getURI(Protocol.HTTP).getPort();
        String bundleUrl = "http://" + getContainerHostIp() + ":" + appPort + "/spiffe-test/bundle";
        createSpiffeIdentityProvider(bundleUrl);
    }

    static String getContainerHostIp() {
        for (String bridge : new String[] { "docker0", "podman0" }) {
            try {
                NetworkInterface iface = NetworkInterface.getByName(bridge);
                if (iface != null) {
                    var addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                // This bridge is not queryable on this host; log it and try the next candidate before
                // falling back to host.docker.internal.
                LOG.debugf(e, "Could not inspect network interface '%s' while resolving the container host IP", bridge);
            }
        }
        return "host.docker.internal";
    }
}
