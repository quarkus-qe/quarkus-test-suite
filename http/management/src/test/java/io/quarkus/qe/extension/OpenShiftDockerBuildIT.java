package io.quarkus.qe.extension;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtensionAndDockerBuildStrategy)
public class OpenShiftDockerBuildIT extends OpenShiftBaseDeploymentIT {
    @Override
    public void health() {
    }
}
