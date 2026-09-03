package io.quarkus.ts.security.pqc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@DisabledOnNative(reason = "PQC is not supported on native yet")
@DisabledOnOs(OS.WINDOWS)
@Tag("QUARKUS-7367")
// TODO jjedlick fix this when back from PTO
@DisabledOnQuarkusSnapshot(reason = "Failing on GH action")
@QuarkusScenario
public class DefaultEngineTestsIT extends AbstractBaseEngineTestsIT {

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureTruststore = true))
    static final RestService app = new RestService()
            .setAutoStart(false);

}
