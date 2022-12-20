package io.quarkus.ts.http.jaxrs.reactive;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;

@Tag("use-quarkus-openshift-extension")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
// OCP Native coverage is not required (Test plan QUARKUS-2487), due to a lack of resources and the ROI.
@DisabledOnNative
// TODO https://github.com/quarkusio/quarkus/issues/29921
@DisabledOnQuarkusSnapshot(reason = "OpenShift extension issue on docker image pushing")
public class OpenShiftCompressionHandlerIT extends CompressionHandlerIT {
}
