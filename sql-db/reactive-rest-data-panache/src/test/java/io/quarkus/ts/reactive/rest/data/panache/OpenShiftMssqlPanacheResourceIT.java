package io.quarkus.ts.reactive.rest.data.panache;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/microsoft/mssql-docker/issues/769")
public class OpenShiftMssqlPanacheResourceIT extends MssqlPanacheResourceIT {
}
