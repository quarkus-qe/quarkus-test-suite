package io.quarkus.ts.langchain4j;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingContainerRegistry)
public class OpenShiftMoviesIT extends MoviesIT {

}
