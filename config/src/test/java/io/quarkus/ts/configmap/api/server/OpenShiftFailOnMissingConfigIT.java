package io.quarkus.ts.configmap.api.server;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftFailOnMissingConfigIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .setAutoStart(false)
            .withProperty("quarkus.kubernetes-config.enabled", "true")
            .withProperty("quarkus.kubernetes-config.config-maps", "absent-config-map");

    @Test
    @Disabled("We can not detect this failure at the moment due to https://github.com/quarkusio/quarkus/issues/38481")
    public void shouldFailOnStart() {
        assertThrows(AssertionError.class, () -> app.start(),
                "Should fail because property 'quarkus.kubernetes-config.fail-on-missing-config' is true and ConfigMap 'absent-config-map' is missing.");
    }

}
