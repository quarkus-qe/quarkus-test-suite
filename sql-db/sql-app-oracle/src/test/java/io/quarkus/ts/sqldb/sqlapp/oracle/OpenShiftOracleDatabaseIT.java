package io.quarkus.ts.sqldb.sqlapp.oracle;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/246")
@OpenShiftScenario
public class OpenShiftOracleDatabaseIT extends OracleDatabaseIT {
}
