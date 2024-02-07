package io.quarkus.ts.http.jakartarest.reactive;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@Tag("use-quarkus-openshift-extension")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
// OCP Native coverage is not required (Test plan QUARKUS-2487), due to a lack of resources and the ROI.
@DisabledOnNative
public class OpenShiftCompressionHandlerIT extends CompressionHandlerIT {
}
