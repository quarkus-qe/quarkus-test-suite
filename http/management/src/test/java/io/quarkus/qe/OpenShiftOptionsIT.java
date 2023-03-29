package io.quarkus.qe;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftOptionsIT extends LocalOptionsIT {
    @Test
    @Override
    @Disabled("SSL on openshift is not supported by the FW (yet)")
    public void tls() {
    }
}
