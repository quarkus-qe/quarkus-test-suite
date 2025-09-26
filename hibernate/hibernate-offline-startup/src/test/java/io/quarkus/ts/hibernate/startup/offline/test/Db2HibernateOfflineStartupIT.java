package io.quarkus.ts.hibernate.startup.offline.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

// note: it is unclear if this test requires more work to pass until the linked issue is fixed
@Disabled("https://github.com/quarkusio/quarkus/issues/50209")
@Tag("fips-incompatible") // Reported in https://github.com/IBM/Db2/issues/43
@Tag("podman-incompatible") //TODO: https://github.com/containers/podman/issues/16432
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2020")
@QuarkusScenario
public class Db2HibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed", builder = FixedPortResourceBuilder.class)
    static final Db2Service db = new Db2Service().setAutoStart(false);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-db2"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:db2://localhost:50000/mydb");
}
