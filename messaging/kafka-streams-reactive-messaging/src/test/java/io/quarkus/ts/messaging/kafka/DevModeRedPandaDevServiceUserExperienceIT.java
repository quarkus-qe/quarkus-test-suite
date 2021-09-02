package io.quarkus.ts.messaging.kafka;

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
public class DevModeRedPandaDevServiceUserExperienceIT {

    private static final String RED_PANDA_VERSION = "latest";
    private static final String RED_PANDA_IMAGE = "vectorized/redpanda";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.devservices.enabled", Boolean.TRUE.toString())
            .withProperty("quarkus.kafka.devservices.image-name", String.format("%s:%s", RED_PANDA_IMAGE, RED_PANDA_VERSION))
            .onPreStart(s -> DockerUtils.removeImage(RED_PANDA_IMAGE, RED_PANDA_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutRedPandaDevServicePulling() {
        app.logs().assertContains("Pulling docker image: vectorized/redpanda");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for Kafka started");
    }

    @Test
    public void verifyRedPandaImage() {
        Image postgresImg = DockerUtils.getImage(RED_PANDA_IMAGE, RED_PANDA_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                RED_PANDA_IMAGE, RED_PANDA_VERSION));
    }
}
