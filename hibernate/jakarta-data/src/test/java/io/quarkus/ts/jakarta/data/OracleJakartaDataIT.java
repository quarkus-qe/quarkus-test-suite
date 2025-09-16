package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
@QuarkusScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/2631")
public class OracleJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${oracle.image}", port = 1521, expectedLog = "DATABASE IS READY TO USE!")
    static final OracleService database = new OracleService();

    @QuarkusApplication
    static final RestService app = createApp(database, "oracle");
}
