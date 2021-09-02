package io.quarkus.ts.messaging.amqpreactive;

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
public class DevModeAmqpDevServiceUserExperienceIT {

    private static final String AMQP_VERSION = "0.1.3";
    private static final String AMQP_IMAGE = "quay.io/artemiscloud/activemq-artemis-broker";

    /**
     * AMQP must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.amqp.devservices.image-name", String.format("%s:%s", AMQP_IMAGE, AMQP_VERSION))
            .onPreStart(s -> DockerUtils.removeImage(AMQP_IMAGE, AMQP_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutAmqpDevServicePulling() {
        app.logs().assertContains("Pulling docker image: quay.io/artemiscloud/activemq-artemis-broker");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for AMQP started");
    }

    @Test
    public void verifyAmqpImage() {
        Image postgresImg = DockerUtils.getImage(AMQP_IMAGE, AMQP_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.keycloak.devservices.image-name' property",
                AMQP_IMAGE, AMQP_VERSION));
    }
}
