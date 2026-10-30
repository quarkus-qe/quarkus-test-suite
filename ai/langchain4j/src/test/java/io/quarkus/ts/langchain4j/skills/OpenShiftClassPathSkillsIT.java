package io.quarkus.ts.langchain4j.skills;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.Build)
public class OpenShiftClassPathSkillsIT extends ClasspathSkillsIT {

}
