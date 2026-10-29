package io.quarkus.ts.langchain4j.skills;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.Build)
@Tag("QUARKUS-8960")
public class OpenShiftClassPathSkillsIT extends ClasspathSkillsIT {

}
