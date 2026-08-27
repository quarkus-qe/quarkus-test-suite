package io.quarkus.ts.security.pqc;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.opentest4j.AssertionFailedError;

import io.netty.handler.ssl.OpenSsl;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;

public abstract class AbstractBaseEngineTestsIT extends BaseVertX {

    private static final String ERROR_LOG = "PQC enforcement policy %s requires X25519MLKEM768 but the configured SSL engine does not support it";
    private static final String ERROR_LOG_STRICT = String.format(ERROR_LOG, "STRICT");
    private static final String ERROR_LOG_CLIENT_NEGOTIATED = String.format(ERROR_LOG, "CLIENT_NEGOTIATED");

    @LookupService
    static RestService app;

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    @EnabledIf("isOpenSsl35Available")
    void testQuarkusAppThrowErrorWithStrict() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "strict").start();
        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello");
    }

    @Test
    @EnabledIf("isOpenSsl35Available")
    void testQuarkusAppThrowErrorWithClientNegotiated() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "client-negotiated").start();
        sendRequest(List.of("X25519MLKEM768"), app.getURI(Protocol.HTTPS) + "/hello");
    }

    @Test
    void testQuarkusAppThrowErrorWithRelaxed() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "relaxed").start();
        sendRequest(List.of("x25519"), app.getURI(Protocol.HTTPS) + "/hello");
    }

    @Test
    @DisabledIf("isOpenSsl35Available")
    void testQuarkusAppThrowErrorWithStrictWithNotAvailable35openSsl() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "strict");
        assertThrows(AssertionFailedError.class, () -> app.start(),
                "OpenSSL engine is not available, the Quarkus start should fail");
        app.logs().assertContains(ERROR_LOG_STRICT);
    }

    @Test
    @DisabledIf("isOpenSsl35Available")
    void testQuarkusAppThrowErrorWithClientNegotiatedWithNotAvailable35openSsl() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "client-negotiated");
        assertThrows(AssertionFailedError.class, () -> app.start(),
                "OpenSSL engine is not available, the Quarkus start should fail");
        app.logs().assertContains(ERROR_LOG_CLIENT_NEGOTIATED);
    }

    static boolean isOpenSsl35Available() {
        return OpenSsl.isAvailable() && OpenSsl.version() >= 0x30500000L;
    }
}
