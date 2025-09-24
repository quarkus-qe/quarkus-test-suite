package io.quarkus.qe.hibernate;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-6243")
public class DevModePostgresqlHQLConsoleIT extends AbstractHQLConsoleIT {

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();
}
