package io.quarkus.ts.http.minimum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("QUARKUS-7656")
@Tag("use-quarkus-openshift-extension")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftCustomImageNameIT extends HttpMinimumIT {

    private static final String CUSTOM_IMAGE_NAME = "custom-image-name";
    private static final String APP_NAME = "app";

    @Inject
    static OpenShiftClient openshift;

    @Test
    public void verifyImageStreamUsesCustomImageName() {
        var is = openshift.getFabric8Client().imageStreams()
                .withName(CUSTOM_IMAGE_NAME).get();
        assertNotNull(is, "ImageStream should exist with name " + CUSTOM_IMAGE_NAME);
        assertEquals(CUSTOM_IMAGE_NAME, is.getMetadata().getName());
    }

    @Test
    public void verifyBuildConfigOutputReferencesCustomImageName() {
        var bc = openshift.getFabric8Client().buildConfigs()
                .withName(APP_NAME).get();
        assertNotNull(bc, "BuildConfig should exist with name " + APP_NAME);
        assertTrue(bc.getSpec().getOutput().getTo().getName()
                .startsWith(CUSTOM_IMAGE_NAME + ":"),
                "BuildConfig output should reference " + CUSTOM_IMAGE_NAME);
    }
}
