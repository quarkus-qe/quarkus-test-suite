package io.quarkus.ts.reactive.openshift;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.ts.reactive.MySQLDatabaseIT;

@OpenShiftScenario
//TODO: enable disabled methods then the issue will be fixed
public class OpenShiftMySqlIT extends MySQLDatabaseIT {

    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/21700")
    public void connectToUniEndpoint() {

    }

    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/21700")
    public void connectToMultiEndpoint() {
    }

}
