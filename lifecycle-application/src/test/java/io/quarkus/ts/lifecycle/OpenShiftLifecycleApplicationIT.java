package io.quarkus.ts.lifecycle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftLifecycleApplicationIT extends LifecycleApplicationIT {

    private static final List<String> EXPECTED_ARGUMENTS = Arrays.asList("ARG1", "ARG2");

    @Test
    public void shouldReturnCustomArguments() {
        String actualArguments = app.given().get("/args")
                .then().statusCode(HttpStatus.SC_OK).extract().asString();
        // From endpoint
        assertExpectedArguments(actualArguments);
        // From logs
        assertExpectedArguments(app.getLogs().stream().collect(Collectors.joining()));
    }

    private void assertExpectedArguments(String actualArguments) {
        EXPECTED_ARGUMENTS.forEach(arg -> assertTrue(actualArguments.contains(arg),
                "Expected argument " + arg + " was not found in actual arguments: " + actualArguments));
    }
}
