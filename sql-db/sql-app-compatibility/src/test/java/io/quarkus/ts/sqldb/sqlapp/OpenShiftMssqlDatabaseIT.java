package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/microsoft/mssql-docker/issues/769")
public class OpenShiftMssqlDatabaseIT extends MssqlDatabaseIT {

}
