package io.quarkus.ts.security.pqc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@Disabled("https://github.com/quarkusio/quarkus/issues/56275")
@DisabledOnNative(reason = "PQC is not supported on native yet")
@Tag("QUARKUS-7367")
@OpenShiftScenario
public class OpenShiftOpenSslDefaultTlsRegistryP12IT extends AbstractOpenSslTlsRegistryIT {

    @QuarkusApplication(ssl = true, properties = "openssl.properties", certificates = @Certificate(configureKeystore = true, configureTruststore = true, configureHttpServer = true, format = Certificate.Format.PKCS12))
    static final RestService app = new RestService().setAutoStart(false);

}
