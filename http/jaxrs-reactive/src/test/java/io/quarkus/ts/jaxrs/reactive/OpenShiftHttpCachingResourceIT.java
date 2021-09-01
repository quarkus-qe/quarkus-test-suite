package io.quarkus.ts.jaxrs.reactive;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftHttpCachingResourceIT extends HttpCachingResourceIT {
}
