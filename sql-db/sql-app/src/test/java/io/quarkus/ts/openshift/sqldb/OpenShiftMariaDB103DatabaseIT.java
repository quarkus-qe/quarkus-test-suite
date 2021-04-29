package io.quarkus.ts.openshift.sqldb;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMariaDB103DatabaseIT extends MariaDB103DatabaseIT {
}
