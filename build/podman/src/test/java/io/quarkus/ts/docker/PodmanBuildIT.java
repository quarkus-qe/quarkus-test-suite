package io.quarkus.ts.docker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.utils.DockerUtils;

@QuarkusScenario
@EnabledIfEnvironmentVariable(named = "DOCKER_HOST", matches = ".*podman.*")
/**
 * We run the tests only when podman is enabled and does not pretend to be Docker
 * since otherwise we won't be able to distinguish between the extension being broken
 * and a situation, when podman is not installed.
 *
 * Unfortunately, that means, that the test is disabled on Windows and Mac
 */
public class PodmanBuildIT {
    // Local container build, no need in tracking image in properties
    private static final String IMAGE_NAME = "hello-world-podman-app";
    private static final String IMAGE_VERSION = "1.0.0";

    @AfterAll
    public static void tearDown() {
        DockerUtils.removeImage(IMAGE_NAME, IMAGE_VERSION);
    }

    @Test
    public void verifyImageNameWithSpaces() {
        Image image = DockerUtils.getImage(IMAGE_NAME, IMAGE_VERSION);
        assertTrue(Objects.nonNull(image.getId()) && !image.getId().isEmpty(), "The image was not created");
    }
}
