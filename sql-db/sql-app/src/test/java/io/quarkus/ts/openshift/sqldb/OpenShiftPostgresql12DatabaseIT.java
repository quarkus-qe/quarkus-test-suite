package io.quarkus.ts.openshift.sqldb;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftPostgresql12DatabaseIT extends Postgresql12DatabaseIT {
}
