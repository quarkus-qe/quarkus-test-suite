package io.quarkus.ts.security.keycloak;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "3\\.15\\.1\\..*", reason = "https://issues.redhat.com/browse/QUARKUS-5054")
public class DevModeKeycloakDevServiceUserExperienceIT {

    private static final String KEYCLOAK_VERSION = getImageVersion("keycloak.image");
    private static final String KEYCLOAK_IMAGE = getImageName("keycloak.image");
    private static final String SECRET_KEYS_MISSING = "Secret key for encrypting tokens in a session cookie is missing, auto-generating it";

    /**
     * Keycloak must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.keycloak.devservices.image-name", "${keycloak.image}")
            .withProperty("quarkus.oidc.credentials.secret", "") // we don't want to use client secret for encryption secret
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

    @Tag("QUARKUS-3158")
    @Test
    public void verifyNoPkceAndTokenEncSecretKeyForServiceApp() {
        // service application doesn't use session cookie and PKCE verifier encryption keys, therefore they don't
        // need to be generated and no warning message should be logged
        app.logs().assertDoesNotContain(SECRET_KEYS_MISSING);
    }
}
