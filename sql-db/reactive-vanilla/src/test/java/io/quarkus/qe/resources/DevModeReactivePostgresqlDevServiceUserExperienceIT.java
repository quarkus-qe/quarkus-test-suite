package io.quarkus.qe.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@Tag("QUARKUS-1080")
@QuarkusScenario
public class DevModeReactivePostgresqlDevServiceUserExperienceIT {
    private static final String POSTGRESQL_VERSION = "9.6.23";
    private static final String POSTGRES_NAME = "postgres";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.devservices.image-name", POSTGRES_NAME + ":" + POSTGRESQL_VERSION)
            .onPreStart(s -> DockerUtils.removeImage(POSTGRES_NAME, POSTGRESQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutReactivePostgresqlDevServicePulling() {
        app.logs().assertContains("Pulling docker image: postgres");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for PostgreSQL started");
    }

    @Test
    public void verifyReactivePostgresqlImage() {
        Image postgresImg = DockerUtils.getImage(POSTGRES_NAME, POSTGRESQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                POSTGRES_NAME, POSTGRESQL_VERSION));
    }
}
