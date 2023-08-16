package io.quarkus.ts.http.jakartarest.reactive;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/quarkusio/quarkus/issues/35344")
public class OpenShiftRESTEasyReactiveMultipartIT extends RESTEasyReactiveMultipartIT {
}
