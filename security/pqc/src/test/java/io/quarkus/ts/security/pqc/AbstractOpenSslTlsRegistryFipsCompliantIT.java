package io.quarkus.ts.security.pqc;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
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
public abstract class AbstractOpenSslTlsRegistryFipsCompliantIT extends BaseVertX {

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
    void testStrictWithDefaultAndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangeEnforcementProperty("strict");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(2)
    void testStrictWithDefaultAndClientSecp384r1Only() {
        restartAppAndChangeEnforcementProperty("strict");

        sendRequestAndExpectFailure(List.of("Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(3)
    void testStrictWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(4)
    void testStrictWithDefaultAndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP384r1MLKEM1024", "Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and default groups of the engine

    @Test
    @Order(5)
    void testClientNegotiatedWithDefaultAndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangeEnforcementProperty("client-negotiated");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(6)
    void testClientNegotiatedWithDefaultAndClientSecp384r1Only() {
        sendRequest(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(7)
    void testClientNegotiatedWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(8)
    void testClientNegotiatedWithDefaultAndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP384r1MLKEM1024", "Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server relaxed and default groups of the engine

    @Test
    @Order(9)
    void testRelaxedWithDefaultAndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangeEnforcementProperty("relaxed");

        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(10)
    void testRelaxedWithDefaultAndClientSecp384r1Only() {
        sendRequest(List.of("Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(11)
    void testRelaxedWithDefaultAndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(12)
    void testRelaxedWithDefaultAndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP384r1MLKEM1024", "Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server strict and groups SecP384r1MLKEM1024

    @Test
    @Order(13)
    void testStrictWithSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("strict", "SecP384r1MLKEM1024");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(14)
    void testStrictWithSecP384r1MLKEM1024AndClientSecp384r1Only() {
        sendRequestAndExpectFailure(List.of("Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(15)
    void testStrictWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(16)
    void testStrictWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768ndSecp384r1() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768", "Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server strict and groups Secp384r1, SecP384r1MLKEM1024

    @Test
    @Order(17)
    void testStrictWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("strict", "SecP384r1MLKEM1024,Secp384r1");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(18)
    void testStrictWithSecP384r1MLKEM1024AndSecp384r1AndClientSecp384r1Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(19)
    void testStrictWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(20)
    void testStrictWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP384r1MLKEM1024", "SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and groups SecP384r1MLKEM1024

    @Test
    @Order(21)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("client-negotiated", "SecP384r1MLKEM1024");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(22)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndClientSecp384r1Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(23)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(24)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768AndSecp384r1() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768", "Secp384r1"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server client-negotiated and groups SecP384r1MLKEM1024, SecP384r1

    @Test
    @Order(25)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("client-negotiated", "SecP384r1MLKEM1024,SecP384r1");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(26)
    void testClientNegotiatedSecP384r1MLKEM1024AndSecp384r1AndClientSecp384r1Only() {
        sendRequest(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(27)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(28)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP256r1MLKEM768AndSecp384r1() {
        sendRequest(List.of("SecP256r1MLKEM768", "SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server client-negotiated and groups SecP256r1MLKEM768, SecP384r1MLKEM1024

    @Test
    @Order(29)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("client-negotiated", "SecP256r1MLKEM768,SecP384r1MLKEM1024");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(30)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecp384r1Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(31)
    void testClientNegotiatedWithSecP384r1MLKEM1024AndSecP256r1MLKEM768AndSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequest(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server relaxed and groups SecP384r1MLKEM1024

    @Test
    @Order(32)
    void testRelaxedWithSecP384r1MLKEM1024AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("relaxed", "SecP384r1MLKEM1024");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(33)
    void testRelaxedWithSecP384r1MLKEM1024AndClientSecp384r1Only() {
        sendRequestAndExpectFailure(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(34)
    void testRelaxedWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(35)
    void testRelaxedWithSecP384r1MLKEM1024AndClientSecP256r1MLKEM768AndSecp384r1() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768", "SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello",
                certificate);
    }

    // Server relaxed and groups Secp384r1

    @Test
    @Order(36)
    void testRelaxedWithSecp384r1AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("relaxed", "SecP384r1");

        sendRequestAndExpectFailure(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(37)
    void testRelaxedWithSecp384r1AndClientSecp384r1Only() {
        sendRequest(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(38)
    void testRelaxedWithSecp384r1AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(39)
    void testRelaxedWithSecp384r1AndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP256r1MLKEM768", "SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Server relaxed and groups SecP384r1MLKEM1024 and Secp384r1

    @Test
    @Order(40)
    void testRelaxedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP384r1MLKEM1024Only() {
        restartAppAndChangePqcProperties("relaxed", "SecP384r1MLKEM1024,SecP384r1");

        sendRequest(List.of("SecP384r1MLKEM1024"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(41)
    void testRelaxedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecp384r1Only() {
        sendRequest(List.of("SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(42)
    void testRelaxedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP256r1MLKEM768Only() {
        sendRequestAndExpectFailure(List.of("SecP256r1MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    @Test
    @Order(43)
    void testRelaxedWithSecP384r1MLKEM1024AndSecp384r1AndClientSecP384r1MLKEM1024AndSecp384r1() {
        sendRequest(List.of("SecP256r1MLKEM768", "SecP384r1"), app.getURI(Protocol.HTTPS) + "/hello", certificate);
    }

    // Test startup warning log when using relaxed with PQC algorithm

    @Test
    @Order(44)
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
