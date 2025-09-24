package io.quarkus.qe.hibernate;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-6243")
public class DevModeMySQLHQLConsoleIT extends AbstractHQLConsoleIT {

    @DevModeQuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mysql"))
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperties("mysql.properties");
}
