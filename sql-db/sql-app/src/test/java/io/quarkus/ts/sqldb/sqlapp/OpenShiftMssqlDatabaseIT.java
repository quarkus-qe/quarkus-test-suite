package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;

//TODO https://github.com/quarkus-qe/quarkus-test-suite/issues/793
@DisabledOnQuarkusSnapshot(reason = "Required property is ignored on OCP")
@OpenShiftScenario
public class OpenShiftMssqlDatabaseIT extends MssqlDatabaseIT {

}
