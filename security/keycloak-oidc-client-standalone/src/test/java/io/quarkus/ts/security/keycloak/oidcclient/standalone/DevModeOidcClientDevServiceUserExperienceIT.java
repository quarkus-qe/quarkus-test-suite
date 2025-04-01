package io.quarkus.ts.security.keycloak.oidcclient.standalone;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@Tag("QUARKUS-5611")
@QuarkusScenario
public class DevModeOidcClientDevServiceUserExperienceIT {

    private static final String KEYCLOAK_VERSION = getImageVersion("keycloak.image");
    private static final String KEYCLOAK_IMAGE_NAME = getImageName("keycloak.image");
    private static final String KEYCLOAK_IMAGE = KEYCLOAK_IMAGE_NAME + ":" + KEYCLOAK_VERSION;

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.keycloak.devservices.image-name", "${keycloak.image}");

    @Test
    public void verifyIfUserIsInformedAboutKeycloakDevServicePullingAndIsStarted() {
        app.logs().assertContains("Creating container for image: " + KEYCLOAK_IMAGE);
        app.logs().assertContains("Container " + KEYCLOAK_IMAGE + " is starting:");
        app.logs().assertContains("Dev Services for Keycloak started");
    }

    @Test
    public void verifyKeycloakImage() {
        Image keycloakImg = DockerUtils.getImage(KEYCLOAK_IMAGE_NAME, KEYCLOAK_VERSION);
        Assertions.assertFalse(keycloakImg.getId().isEmpty(), String.format("%s:%s not found. "
                + "Overwriting`quarkus.keycloak.devservices.image-name` default value not worked.",
                KEYCLOAK_IMAGE, KEYCLOAK_VERSION));
    }
}
