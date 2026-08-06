package io.quarkus.ts.http.restclient.reactive;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("QUARKUS-8369")
@OpenShiftScenario
public class OpenShiftRestMultiResponseIT extends RestMultiResponseIT {

}
