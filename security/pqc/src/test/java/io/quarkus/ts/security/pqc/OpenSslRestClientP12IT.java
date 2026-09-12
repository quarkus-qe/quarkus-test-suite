package io.quarkus.ts.security.pqc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.Protocol;
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
public class OpenSslRestClientP12IT extends AbstractOpenSslRestClientIT {

    @QuarkusApplication(ssl = true, properties = "openssl.properties", certificates = @Certificate(configureKeystore = true, configureTruststore = true, format = Certificate.Format.PKCS12))
    static final RestService server = new RestService()
            .setAutoStart(false);

    @QuarkusApplication(ssl = true, properties = "rest-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.rest-client.hello-api.url", () -> server.getURI(Protocol.HTTPS).toString())
            .withProperty("quarkus.tls.pqc.trust-store.p12.path", AbstractOpenSslRestClientIT::serverTruststorePath)
            .withProperty("quarkus.tls.pqc.trust-store.p12.password", CERT_PASSWORD)
            .setAutoStart(false);
}
