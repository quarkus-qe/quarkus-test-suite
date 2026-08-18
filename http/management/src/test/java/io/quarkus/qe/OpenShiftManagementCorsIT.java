package io.quarkus.qe;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("QUARKUS-7819")
@OpenShiftScenario
public class OpenShiftManagementCorsIT extends ManagementCorsIT {

}
