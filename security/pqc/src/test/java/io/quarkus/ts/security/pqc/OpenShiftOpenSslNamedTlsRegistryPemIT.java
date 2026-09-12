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
public class OpenShiftOpenSslNamedTlsRegistryPemIT extends AbstractOpenSslTlsRegistryIT {

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
