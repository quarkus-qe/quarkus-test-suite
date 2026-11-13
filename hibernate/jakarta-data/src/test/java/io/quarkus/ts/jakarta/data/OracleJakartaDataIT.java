package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO Quarkus 4: Jakarta Data repositories without dataStore register named queries in the
// default (unnamed) PU at build time. With the named-PU config (using active toggling per profile),
// the default PU doesn't exist and the queries are not found. The RepositoryBeansProducer
// runtime redirect no longer works because query registration happens at build time.
@Disabled("TODO Quarkus 4: Jakarta Data named query registration requires default PU")
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
@QuarkusScenario
public class OracleJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${oracle.image}", port = 1521, expectedLog = "DATABASE IS READY TO USE!")
    static final OracleService database = new OracleService();

    @QuarkusApplication
    static final RestService app = createApp(database, "oracle");
}
