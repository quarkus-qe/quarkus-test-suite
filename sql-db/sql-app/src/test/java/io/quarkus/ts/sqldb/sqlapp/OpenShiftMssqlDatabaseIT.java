package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@Disabled("https://github.com/microsoft/mssql-docker/issues/769")
public class OpenShiftMssqlDatabaseIT extends MssqlDatabaseIT {

}
