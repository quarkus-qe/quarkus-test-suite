package io.quarkus.ts.openshift.sqldb.multiplepus;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftMultiplePersistenceIT extends MultiplePersistenceIT {
}
