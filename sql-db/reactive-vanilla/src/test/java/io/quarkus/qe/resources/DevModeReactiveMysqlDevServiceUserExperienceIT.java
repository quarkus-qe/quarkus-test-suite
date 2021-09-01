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
public class DevModeReactiveMysqlDevServiceUserExperienceIT {
    private static final String MYSQL_VERSION = "5.6.51";
    private static final String MYSQL_NAME = "mysql";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.mysql.devservices.image-name", MYSQL_NAME + ":" + MYSQL_VERSION)
            .onPreStart(s -> DockerUtils.removeImage(MYSQL_NAME, MYSQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutReactivePostgresqlDevServicePulling() {
        app.logs().assertContains("Pulling docker image: mysql");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for MySQL started");
    }

    @Test
    public void verifyReactivePostgresqlImage() {
        Image postgresImg = DockerUtils.getImage(MYSQL_NAME, MYSQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MYSQL_NAME, MYSQL_VERSION));
    }
}
