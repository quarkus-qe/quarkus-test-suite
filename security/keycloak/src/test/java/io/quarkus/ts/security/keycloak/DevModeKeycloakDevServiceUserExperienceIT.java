package io.quarkus.ts.security.keycloak;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeKeycloakDevServiceUserExperienceIT {

    private static final String KEYCLOAK_VERSION = "13.0.1";
    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak";

    /**
     * Keycloak must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.keycloak.devservices.image-name", String.format("%s:%s", KEYCLOAK_IMAGE, KEYCLOAK_VERSION))
            .onPreStart(s -> DockerUtils.removeImage(KEYCLOAK_IMAGE, KEYCLOAK_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutKeycloakDevServicePulling() {
        app.logs().assertContains("Pulling docker image: quay.io/keycloak/keycloak");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for Keycloak started");
    }

    @Test
    public void verifyKeycloakImage() {
        Image postgresImg = DockerUtils.getImage(KEYCLOAK_IMAGE, KEYCLOAK_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.keycloak.devservices.image-name' property",
                KEYCLOAK_IMAGE, KEYCLOAK_VERSION));
    }
}
