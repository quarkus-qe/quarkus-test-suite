package io.quarkus.ts.spring.data.rest.additional;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://redhat.atlassian.net/browse/QUARKUS-9648")
public class OpenShiftOpenAPIIT extends OpenAPIIT {
}
