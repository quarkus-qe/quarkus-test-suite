package io.quarkus.ts.http.restclient.reactive;

import static hero.TlsCertificateResource.HERO_CLIENT_CN;
import static hero.TlsCertificateResource.HERO_CLIENT_CN_2;
import static io.quarkus.test.services.Certificate.Format.PEM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.security.certificate.ClientCertificateRequest;
import io.quarkus.test.security.certificate.PemClientCertificate;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

import hero.CertificateInfo;
import hero.Hero;
import hero.HeroClient;
import hero.HeroClientResource;
import hero.HeroResource;
import hero.TlsCertificateClient;
import hero.TlsCertificateClientResource;
import hero.TlsCertificateResource;
import hero.Villain;
import hero.VillainClient;
import hero.VillainClientResource;
import hero.VillainResource;

@Tag("QUARKUS-5866")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // enforce order so that the certificate reload happens last
@QuarkusScenario
public class QuarkusRestClientMutualTlsIT {

    private static final String CERTIFICATE_PREFIX = "certificate-reload";
    private static final String SERVER_TLS_CONFIG_NAME = "cert-serving-test-server";

    @QuarkusApplication(ssl = true, certificates = @Certificate(tlsConfigName = SERVER_TLS_CONFIG_NAME, prefix = CERTIFICATE_PREFIX, clientCertificates = {
            @Certificate.ClientCertificate(cnAttribute = HERO_CLIENT_CN)
    }, format = PEM, configureHttpServer = true, configureTruststore = true, configureKeystore = true), classes = {
            HeroResource.class, Hero.class, Villain.class, CertificateInfo.class, VillainResource.class,
            TlsCertificateResource.class }, properties = "certificate-serving-server.properties")
    static final RestService server = new RestService()
            .withProperty("quarkus.http.ssl.client-auth", "required");

    @QuarkusApplication(classes = {
            HeroClient.class, Hero.class, HeroClientResource.class, Villain.class, VillainClient.class,
            VillainClientResource.class, CertificateInfo.class, TlsCertificateClient.class,
            TlsCertificateClientResource.class }, properties = "certificate-serving-client.properties")
    static final RestService client = new RestService()
            .withProperties(() -> {
                var pemClientCertificate = getClientPemCertificate();
                return Map.of(
                        "quarkus.tls.tls-certificate-client.key-store.pem.pem-1.cert",
                        "${quarkus.tls.hero-client.key-store.pem.pem-1.cert}",
                        "quarkus.tls.tls-certificate-client.key-store.pem.pem-1.key",
                        "${quarkus.tls.hero-client.key-store.pem.pem-1.key}",
                        "quarkus.tls.hero-client.key-store.pem.pem-1.cert",
                        doubleBackSlashesOnWin(pemClientCertificate.certPath()),
                        "quarkus.tls.hero-client.key-store.pem.pem-1.key",
                        doubleBackSlashesOnWin(pemClientCertificate.keyPath()),
                        "quarkus.tls.hero-client.trust-store.pem.certs",
                        doubleBackSlashesOnWin(pemClientCertificate.truststorePath()));
            })
            .withProperty("quarkus.rest-client.hero.uri", () -> server.getURI(Protocol.HTTPS).toString())
            .withProperty("test.tls-certificate.expected-cn", HERO_CLIENT_CN);

    @Order(1)
    @Test
    public void testMutualTlsBetweenClientAndServer() {
        var hero = client.given().get("hero-client-resource").then().statusCode(200).extract().as(Hero.class);
        assertNotNull(hero);
        assertNotNull(hero.name());
        assertTrue(hero.name().startsWith("Name-"));
        assertNotNull(hero.otherName());
        assertTrue(hero.otherName().startsWith("Other-"));
    }

    @Order(2)
    @Test
    public void testConfiguredTlsProtocolEnforced() {
        client.logs().assertDoesNotContain("Received fatal alert: protocol_version");
        client.given().get("villain-client-resource").then().statusCode(500);
        client.logs().assertContains("Received fatal alert: protocol_version");
    }

    @Order(3)
    @Test
    public void testRestClientCertificateReloading() {
        // this test overrides the REST client base URL so that when we restart the 'server' service, target port correct

        var certificateInfo = client.given().queryParam("url", server.getURI(Protocol.HTTPS).toString())
                .get("/tls-certificate/certificate-info-url-override").then().statusCode(200).extract()
                .as(CertificateInfo.class);
        assertNotNull(certificateInfo);
        assertEquals("CN=" + HERO_CLIENT_CN, certificateInfo.principalName());
        var firstCertificateSerialNumber = certificateInfo.serialNumber();
        assertNotNull(firstCertificateSerialNumber);

        // now change the client certificate, if the certificate reload works properly in the REST client
        // then server must respond with a client that has different CN
        getCertificateBuilder().regenerateCertificate(CERTIFICATE_PREFIX, req -> req
                .withClientRequests(new ClientCertificateRequest(HERO_CLIENT_CN_2, false)));

        // as long as connection between the client and server is active, we will only see the old certificates
        // as the new certificates are only used next time we open a connection; hence the restart
        Log.info("Restarting '%s' service so that connection with '%s' service is disconnected", server.getName(),
                client.getName());
        server.restart();

        AwaitilityUtils.untilAsserted(() -> {
            var newCertificateInfo = client.given().queryParam("url", server.getURI(Protocol.HTTPS).toString())
                    .get("/tls-certificate/certificate-info-url-override").then().statusCode(200).extract()
                    .as(CertificateInfo.class);
            assertNotNull(newCertificateInfo);
            assertEquals("CN=" + HERO_CLIENT_CN_2, newCertificateInfo.principalName());
            var secondCertificateSerialNumber = newCertificateInfo.serialNumber();
            assertNotNull(secondCertificateSerialNumber);
            assertNotEquals(firstCertificateSerialNumber, secondCertificateSerialNumber);
        });
    }

    private static PemClientCertificate getClientPemCertificate() {
        return getClientPemCertificate(getGeneratedCertificate(), HERO_CLIENT_CN);
    }

    private static PemClientCertificate getClientPemCertificate(io.quarkus.test.security.certificate.Certificate certificate,
            String cn) {
        return (PemClientCertificate) certificate.getClientCertificateByCn(cn);
    }

    private static CertificateBuilder getCertificateBuilder() {
        return server.getPropertyFromContext(CertificateBuilder.INSTANCE_KEY);
    }

    private static io.quarkus.test.security.certificate.Certificate getGeneratedCertificate() {
        return getCertificateBuilder().findCertificateByPrefix(CERTIFICATE_PREFIX);
    }

    private static String doubleBackSlashesOnWin(String value) {
        if (OS.WINDOWS.isCurrentOs()) {
            // we need to quote back slashes passed as command lines in Windows as they have special meaning
            // TODO: move this to FW, this is duplicated code among this TS
            //   see also io.quarkus.test.security.certificate.Certificate.doubleBackSlashesOnWin
            return value.replace("\\", "\\\\");
        }
        return value;
    }
}
