package io.quarkus.qe.extension;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtensionAndDockerBuildStrategy)
public class OpenShiftDockerBuildIT extends OpenShiftBaseDeploymentIT {
    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/34645")
    public void health() {
    }
}
