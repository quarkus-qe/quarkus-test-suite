package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.Certificate.ServingCertificates;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.Command;

import hero.Hero;
import hero.HeroClient;
import hero.HeroClientResource;
import hero.HeroResource;
import hero.Villain;
import hero.VillainClient;
import hero.VillainClientResource;
import hero.VillainResource;

/**
 * Test OpenShift serving certificate support and Quarkus REST client.
 */
@Tag("QUARKUS-4592")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@OpenShiftScenario
public class OpenShiftServingCertificatesIT {

    private static final String HERO_CLIENT = "hero-client";
    private static final String SERVER_TLS_CONFIG_NAME = "cert-serving-test-server";

    @Inject
    static OpenShiftClient ocp;

    @QuarkusApplication(ssl = true, certificates = @Certificate(tlsConfigName = SERVER_TLS_CONFIG_NAME, servingCertificates = {
            @ServingCertificates(addServiceCertificate = true)
    }), classes = { HeroResource.class, Hero.class, Villain.class,
            VillainResource.class }, properties = "certificate-serving-server.properties")
    static final RestService server = new RestService();

    @QuarkusApplication(certificates = @Certificate(tlsConfigName = HERO_CLIENT, servingCertificates = @ServingCertificates(injectCABundle = true)), classes = {
            HeroClient.class, Hero.class, HeroClientResource.class, Villain.class, VillainClient.class,
            VillainClientResource.class }, properties = "certificate-serving-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.rest-client.hero.uri", () -> server.getURI(Protocol.HTTPS).getRestAssuredStyleUri());

    @Order(1)
    @Test
    public void testSecuredCommunicationBetweenClientAndServer() {

        // REST client use OpenShift internal CA
        // server is configured with OpenShift serving certificates
        // ad "untilAsserted": we experienced unknown SAN, so to avoid flakiness I am adding here retry:
        try {
            var hero = client.given().get("hero-client-resource").then().statusCode(200).extract().as(Hero.class);
            assertNotNull(hero);
            assertNotNull(hero.name());
            assertTrue(hero.name().startsWith("Name-"));
            assertNotNull(hero.otherName());
            assertTrue(hero.otherName().startsWith("Other-"));
        } catch (Throwable t) {
            // FIXME: debug only, don't merge this
            runOcpCmd("oc", "get", "pod", "-o", "wide");
            runOcpCmd("oc", "describe", "secret", "serving-certificates-secret");
            runOcpCmd("oc", "describe", "configmap", "ca-bundle-configmap");
            ocp.podsInService(server).forEach(pod -> runOcpCmd("oc", "describe", "pod", pod.getMetadata().getName()));
            ocp.podsInService(client).forEach(pod -> runOcpCmd("oc", "describe", "pod", pod.getMetadata().getName()));
            throw t;
        }
    }

    @Order(2)
    @Test
    public void testConfiguredTlsProtocolEnforced() {
        // verifies that protocol version set in TLS config is obliged by both HTTP server and client
        // REST client requires TLSv1.2
        // HTTP server requires TLSv1.3
        client.logs().assertDoesNotContain("Received fatal alert: protocol_version");
        client.given().get("villain-client-resource").then().statusCode(500);
        client.logs().assertContains("Received fatal alert: protocol_version");
    }

    private static void runOcpCmd(String... commands) {
        try {
            new Command(commands).outputToConsole().runAndWait();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
