package io.quarkus.qe.properties.toggle;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.restassured.specification.RequestSpecification;

public abstract class BaseToggleablePropertiesIT {

    protected abstract void whenChangeServiceAtRuntime(ToggleableServices service, boolean enable);

    protected abstract RequestSpecification given();

    @ParameterizedTest
    @EnumSource(ToggleableServices.class)
    public void shouldBeUpAndRunning(ToggleableServices service) {
        thenServiceIsRunning(service);

        whenDisableServiceAtRuntime(service);
        thenServiceIsNotRunning(service);

        whenEnableServiceAtRuntime(service);
        thenServiceIsRunning(service);
    }

    private void whenDisableServiceAtRuntime(ToggleableServices service) {
        whenChangeServiceAtRuntime(service, false);
    }

    private void whenEnableServiceAtRuntime(ToggleableServices service) {
        whenChangeServiceAtRuntime(service, true);
    }

    private void thenServiceIsRunning(ToggleableServices service) {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            given().get(service.getEndpoint())
                    .then().statusCode(HttpStatus.SC_OK);
        });
    }

    private void thenServiceIsNotRunning(ToggleableServices service) {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            given().get(service.getEndpoint())
                    .then().statusCode(HttpStatus.SC_NOT_FOUND);
        });
    }
}
