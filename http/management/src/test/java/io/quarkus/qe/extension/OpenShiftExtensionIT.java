package io.quarkus.qe.extension;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftExtensionIT extends OpenShiftBaseDeploymentIT {
    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/34645")
    public void health() {
    }
}
