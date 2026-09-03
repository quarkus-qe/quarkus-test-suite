package io.quarkus.ts.security.pqc;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.opentest4j.AssertionFailedError;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@DisabledOnNative(reason = "PQC is not supported on native yet")
@DisabledOnOs(OS.WINDOWS)
@Tag("QUARKUS-7367")
@QuarkusScenario
public class JdkSslEngineTestsIT extends BaseVertX {

    private static final String ERROR_LOG = "PQC enforcement policy %s requires X25519MLKEM768 but the configured SSL engine does not support it";
    private static final String ERROR_LOG_STRICT = String.format(ERROR_LOG, "STRICT");
    private static final String ERROR_LOG_CLIENT_NEGOTIATED = String.format(ERROR_LOG, "CLIENT_NEGOTIATED");

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureTruststore = true))
    static final RestService app = new RestService()
            .withProperty("quarkus.tls.ssl-engine", "jdkssl")
            .setAutoStart(false);

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void testQuarkusAppThrowErrorWithStrict() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "strict");
        assertThrows(AssertionFailedError.class, () -> app.start(),
                "JdkSsl engine not support PQC yet, the Quarkus start should fail");
        app.logs().assertContains(ERROR_LOG_STRICT);
    }

    @Test
    void testQuarkusAppThrowErrorWithClientNegotiated() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "client-negotiated");
        assertThrows(AssertionFailedError.class, () -> app.start(),
                "JdkSsl engine not support PQC yet, the Quarkus start should fail");
        app.logs().assertContains(ERROR_LOG_CLIENT_NEGOTIATED);
    }

    @Test
    // TODO jjedlick fix this when back from PTO
    @DisabledOnQuarkusSnapshot(reason = "Failing on GH action")
    void testQuarkusAppThrowErrorWithRelaxed() {
        app.withProperty("quarkus.tls.pqc-enforcement-policy", "relaxed").start();
        sendRequest(List.of("x25519"), app.getURI(Protocol.HTTPS) + "/hello");
    }
}
