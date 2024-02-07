package io.quarkus.qe.extension;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftExtensionIT extends OpenShiftBaseDeploymentIT {
    @Override
    public void health() {
    }
}
