package io.quarkus.ts.vertx;

import static io.quarkus.test.services.Certificate.Format.PEM;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnQuarkusVersion(version = "3\\.(8|9|10|11|12|13|14)\\..*|3\\.15\\.(0|1)(\\..*)?", reason = "Disabled on Quarkus versions before 3.15.2 due to known port conflict issue https://github.com/quarkusio/quarkus/issues/43373")
public class PortConflictIT {

    static final int COMMON_PORT_HTTP_HTTPS = 50000;
    @QuarkusApplication(ssl = true, certificates = @Certificate(format = PEM, configureHttpServer = true, configureKeystore = true, configureTruststore = true))
    static RestService app = new RestService()
            .setAutoStart(false)
            .withProperty("quarkus.http.port", String.valueOf(COMMON_PORT_HTTP_HTTPS))
            .withProperty("quarkus.http.ssl-port", String.valueOf(COMMON_PORT_HTTP_HTTPS));

    @Test
    void verifyAppBehaviourUsingSamePorts() {
        assertThrows(AssertionError.class, () -> app.start(),
                "Should fail because Both http and https servers started on port 50000");
        String logs = app.getLogs().toString();
        assertTrue(logs.contains("Both http and https servers started on port 50000"));
    }

}
