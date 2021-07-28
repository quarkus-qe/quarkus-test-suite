package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMariaDB103DatabaseIT extends MariaDB103DatabaseIT {
}
