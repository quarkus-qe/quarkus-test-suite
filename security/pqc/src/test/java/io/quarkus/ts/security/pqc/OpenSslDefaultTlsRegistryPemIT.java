package io.quarkus.ts.security.pqc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.pqc.annotations.DisabledOnSsl35AndLower;

@DisabledOnNative(reason = "PQC is not supported on native yet")
@DisabledOnOs(OS.WINDOWS)
@DisabledOnSsl35AndLower(reason = "PQC is not supported on system which don't have OpenSSL 3.5+")
@Tag("QUARKUS-7367")
@QuarkusScenario
public class OpenSslDefaultTlsRegistryPemIT extends AbstractOpenSslTlsRegistryIT {

    @QuarkusApplication(ssl = true, properties = "openssl.properties", certificates = @Certificate(configureKeystore = true, configureTruststore = true, configureHttpServer = true, format = Certificate.Format.PEM))
    static final RestService app = new RestService().setAutoStart(false);

}
