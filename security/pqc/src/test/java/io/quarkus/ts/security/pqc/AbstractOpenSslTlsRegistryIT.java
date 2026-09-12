package io.quarkus.ts.security.pqc;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.security.certificate.Certificate;
import io.quarkus.test.security.certificate.CertificateBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractOpenSslTlsRegistryIT extends BaseVertX {

    @LookupService
    static RestService app;

    private static Certificate certificate;

    @BeforeAll
    static void init() {
        CertificateBuilder certificateBuilder = (CertificateBuilder) app
                .getPropertyFromContext("io.quarkus.test.security.certificate#INSTANCE");
        if (certificateBuilder == null) {
            throw new IllegalStateException("Certificates should be available");
        }

        certificate = certificateBuilder.certificates().get(0);
    }

    // Server strict and default groups of the engine

    @Test
    @Order(1)
    void testStrictWithDefaultAndClientX25519MLKEM768Only() {
        restartAppAndChangeEnforcementProperty("strict");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(2)
    void testStrictWithDefaultAndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(3)
    void testStrictWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(4)
    void testStrictWithDefaultAndClientSecP384r1MLKEM1024Only() {
        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(5)
    void testStrictWithDefaultAndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and default groups of the engine

    @Test
    @Order(6)
    void testClientNegotiatedWithDefaultAndClientX25519MLKEM768Only() {
        restartAppAndChangeEnforcementProperty("client-negotiated");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(7)
    void testClientNegotiatedWithDefaultAndClientX25519Only() {
        sendRequest(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(8)
    void testClientNegotiatedWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(9)
    void testClientNegotiatedWithDefaultAndClientSecP384r1MLKEM1024Only() {
        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(10)
    void testClientNegotiatedWithDefaultAndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server relaxed and default groups of the engine

    @Test
    @Order(11)
    void testRelaxedWithDefaultAndClientX25519MLKEM768Only() {
        restartAppAndChangeEnforcementProperty("relaxed");

        sendRequestAndExpectFailure(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(12)
    void testRelaxedWithDefaultAndClientX25519Only() {
        sendRequest(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(13)
    void testRelaxedWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(14)
    void testRelaxedWithDefaultAndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(15)
    void testRelaxedWithDefaultAndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server strict and groups X25519MLKEM768

    @Test
    @Order(16)
    void testStrictWithX25519MLKEM768AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("strict", "X25519MLKEM768");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(17)
    void testStrictWithX25519MLKEM768AndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(18)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(19)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(20)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server strict and groups 25519MLKEM768, X25519

    @Test
    @Order(21)
    void testStrictWithX25519MLKEM768AndX25519AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("strict", "X25519MLKEM768,X25519");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(22)
    void testStrictWithX25519MLKEM768AndX25519AndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(23)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndX25519AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(24)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndX25519AndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(25)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testStrictWithX25519MLKEM768AndX25519AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server strict and groups X25519MLKEM768, SecP256r1MLKEM768, SecP384r1MLKEM1024

    @Test
    @Order(26)
    void testStrictWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("strict", "X25519MLKEM768,X25519");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(27)
    void testStrictWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(28)
    void testStrictWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(29)
    void testStrictWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(30)
    void testStrictWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and groups X25519MLKEM768

    @Test
    @Order(31)
    void testClientNegotiatedWithX25519MLKEM768AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("client-negotiated", "X25519MLKEM768");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(32)
    void testClientNegotiatedWithX25519MLKEM768AndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(33)
    void testClientNegotiatedWithX25519MLKEM768AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(34)
    void testClientNegotiatedWithX25519MLKEM768AndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(35)
    void testClientNegotiatedWithX25519MLKEM768AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server client-negotiated and groups X25519MLKEM768, X25519

    @Test
    @Order(36)
    void testClientNegotiatedWithX25519MLKEM768AndX25519AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("client-negotiated", "X25519MLKEM768,X25519");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(37)
    void testClientNegotiatedX25519MLKEM768AndX25519AndClientX25519Only() {
        sendRequest(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(38)
    void testClientNegotiatedWithX25519MLKEM768AndX25519AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(39)
    void testClientNegotiatedWithX25519MLKEM768AndX25519AndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(40)
    void testClientNegotiatedWithX25519MLKEM768AndX25519AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and groups X25519MLKEM768, SecP256r1MLKEM768, SecP384r1MLKEM1024

    @Test
    @Order(41)
    void testClientNegotiatedWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("client-negotiated", "X25519MLKEM768,SecP256r1MLKEM768,SecP384r1MLKEM1024");

        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(42)
    void testClientNegotiatedWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientX25519Only() {
        sendRequestAndExpectFailure(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(43)
    void testClientNegotiatedWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(44)
    void testClientNegotiatedWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(45)
    void testClientNegotiatedWithX25519MLKEM768AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server relaxed and groups X25519MLKEM768

    @Test
    @Order(46)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testRelaxedWithX25519MLKEM768AndClientX25519MLKEM768Only() {
        restartAppAndChangePqcProperties("relaxed", "X25519MLKEM768");

        sendRequestAndExpectFailure(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(47)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testRelaxedWithX25519MLKEM768AndClientX25519Only() {
        sendRequest(List.of("X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(48)
    void testRelaxedWithX25519MLKEM768AndClientSecP256r1MLKEM768Only() {
        // TODO Remove the prepareAppProperties when testRelaxedWithX25519MLKEM768AndClientX25519MLKEM768Only is fixed
        restartAppAndChangePqcProperties("relaxed", "X25519MLKEM768");

        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(49)
    void testRelaxedWithX25519MLKEM768AndClientSecP384r1MLKEM1024Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(50)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    void testRelaxedWithX25519MLKEM768AndClientSecP384r1MLKEM1024AndX25519() {
        sendRequest(List.of("SecP384r1MLKEM1024", "X25519"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(51)
    void testLogWhenGroupsAreSetWithRelaxed() {
        app.logs().assertContains(
                "The post-quantum groups will be ignored because 'relaxed' does not enforce post-quantum key exchange");
    }

    public void restartAppAndChangePqcProperties(String enforcementPolicy, String keyExchangeGroups) {
        app.stop();
        app.withProperty("quarkus.tls.pqc-enforcement-policy", enforcementPolicy)
                .withProperty("quarkus.tls.key-exchange-groups", keyExchangeGroups);
        app.start();
    }

    public void restartAppAndChangeEnforcementProperty(String enforcementPolicy) {
        app.stop();
        app.withProperty("quarkus.tls.pqc-enforcement-policy", enforcementPolicy);
        app.start();
    }
}
