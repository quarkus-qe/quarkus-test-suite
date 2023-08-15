package io.quarkus.ts.http.jakartarest.reactive;

import io.quarkus.test.scenarios.OpenShiftScenario;
import org.junit.jupiter.api.Disabled;

@OpenShiftScenario
@Disabled("https://github.com/quarkusio/quarkus/issues/35344")
public class OpenShiftRESTEasyReactiveMultipartIT extends RESTEasyReactiveMultipartIT {
}
