package io.quarkus.ts.http.jakartarest.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@Disabled("https://github.com/quarkusio/quarkus/issues/38018")
@Tag("use-quarkus-openshift-extension")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftHttpCachingIT extends HttpCachingIT {
}
