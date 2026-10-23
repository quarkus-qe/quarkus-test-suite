package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

// TODO Quarkus 4: Jakarta Data repositories without dataStore register named queries in the
// default (unnamed) PU at build time. With the named-PU config (using active toggling per profile),
// the default PU doesn't exist and the queries are not found. The RepositoryBeansProducer
// runtime redirect no longer works because query registration happens at build time.
@Disabled("TODO Quarkus 4: Jakarta Data named query registration requires default PU")
@QuarkusScenario
@Tag("fips-incompatible") // Reported in https://github.com/IBM/Db2/issues/43
@Tag("podman-incompatible") //TODO: https://github.com/containers/podman/issues/16432
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2020")
public class Db2JakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static final Db2Service db2 = new Db2Service();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-db2"), properties = "db2.properties")
    static final RestService app = createApp(db2, "db2");

}
