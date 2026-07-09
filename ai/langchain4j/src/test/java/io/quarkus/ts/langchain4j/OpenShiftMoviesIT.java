package io.quarkus.ts.langchain4j;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "The newest/3.x SNAPSHOT is not available in public repository.")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "pgvector container not available on s390x & ppc64le.")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.Build)
public class OpenShiftMoviesIT extends MoviesIT {

}
