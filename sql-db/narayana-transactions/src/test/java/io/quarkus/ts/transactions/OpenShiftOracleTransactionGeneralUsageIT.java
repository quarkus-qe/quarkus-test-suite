package io.quarkus.ts.transactions;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/246")
public class OpenShiftOracleTransactionGeneralUsageIT extends OracleTransactionGeneralUsageIT {
}
