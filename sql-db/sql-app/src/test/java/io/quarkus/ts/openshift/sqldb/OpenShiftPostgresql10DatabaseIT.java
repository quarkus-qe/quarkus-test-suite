package io.quarkus.ts.openshift.sqldb;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftPostgresql10DatabaseIT extends Postgresql10DatabaseIT {
}
