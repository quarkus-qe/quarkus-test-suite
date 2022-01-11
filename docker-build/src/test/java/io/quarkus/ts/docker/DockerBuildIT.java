package io.quarkus.ts.docker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.utils.DockerUtils;

@QuarkusScenario
@DisabledOnNative(reason = "To reduce execution time")
public class DockerBuildIT {

    private static final String DOCKER_IMG_NAME = "hello-world-app";
    private static final String DOCKER_IMG_VERSION = "1.0.0";

    @AfterAll
    public static void tearDown() {
        DockerUtils.removeImage(DOCKER_IMG_NAME, DOCKER_IMG_VERSION);
    }

    @Test
    public void buildDockerImage() {
        Image dockerImg = DockerUtils.getImage(DOCKER_IMG_NAME, DOCKER_IMG_VERSION);
        assertTrue(Objects.nonNull(dockerImg.getId()) && !dockerImg.getId().isEmpty(), "Docker image was not created");
    }
}
