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
public class OpenSslNamedTlsRegistryPemIT extends AbstractOpenSslTlsRegistryIT {

    public static final String TLS_CONFIG_NAME = "pqc-named";

    @QuarkusApplication(ssl = true, properties = "openssl.properties", certificates = @Certificate(configureKeystore = true, configureTruststore = true, configureHttpServer = true, format = Certificate.Format.PEM, tlsConfigName = TLS_CONFIG_NAME))
    static final RestService app = new RestService().setAutoStart(false);

    @Override
    public void restartAppAndChangePqcProperties(String enforcementPolicy, String keyExchangeGroups) {
        app.stop();
        app.withProperty(String.format("quarkus.tls.%s.pqc-enforcement-policy", TLS_CONFIG_NAME), enforcementPolicy)
                .withProperty(String.format("quarkus.tls.%s.key-exchange-groups", TLS_CONFIG_NAME), keyExchangeGroups);
        app.start();
    }

    @Override
    public void restartAppAndChangeEnforcementProperty(String enforcementPolicy) {
        app.stop();
        app.withProperty(String.format("quarkus.tls.%s.pqc-enforcement-policy", TLS_CONFIG_NAME), enforcementPolicy);
        app.start();
    }

}
