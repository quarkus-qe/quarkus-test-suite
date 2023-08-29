package io.quarkus.ts.transactions;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftPostgresqlTransactionGeneralUsageIT extends PostgresqlTransactionGeneralUsageIT {

    @Override
    protected void testTransactionRecoveryInternal() {
        // disabled on OpenShift as there are required changes in Test Framework in container restart
    }
}
