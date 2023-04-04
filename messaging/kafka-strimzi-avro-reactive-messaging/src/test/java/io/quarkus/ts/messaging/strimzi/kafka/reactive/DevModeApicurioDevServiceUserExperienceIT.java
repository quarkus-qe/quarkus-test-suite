package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaRegistry;
import io.quarkus.test.utils.DockerUtils;

@Tag("QUARKUS-959")
@Tag("QUARKUS-1087")
@QuarkusScenario
public class DevModeApicurioDevServiceUserExperienceIT {
    private static final String APICURIO_VERSION = KafkaRegistry.APICURIO.getDefaultVersion();
    private static final String APICURIO_IMAGE = "apicurio/apicurio-registry-mem";

    @DevModeQuarkusApplication
    static RestService appDevServices = new RestService()
            .withProperties("devservices-application.properties")
            .withProperty("quarkus.apicurio-registry.devservices.image-name",
                    String.format("%s:%s", APICURIO_IMAGE, APICURIO_VERSION))
            .onPreStart(s -> DockerUtils.removeImage(APICURIO_IMAGE, APICURIO_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutApicurioServicePulling() {
        appDevServices.logs().assertContains("Pulling docker image: apicurio/apicurio-registry-mem");
        appDevServices.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        appDevServices.logs().assertContains("Starting to pull image");
        appDevServices.logs().assertContains("Dev Services for Apicurio Registry started");
    }

    @Test
    public void verifyApicurioImage() {
        Image postgresImg = DockerUtils.getImage(APICURIO_IMAGE, APICURIO_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.apicurio-registry.devservices.image-name' property",
                APICURIO_IMAGE, APICURIO_VERSION));
    }
}
