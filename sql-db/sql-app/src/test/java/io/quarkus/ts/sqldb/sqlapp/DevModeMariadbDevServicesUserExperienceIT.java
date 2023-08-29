package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;
import io.quarkus.test.utils.SocketUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMariadbDevServicesUserExperienceIT {

    private static final String MARIA_DB_NAME = DbUtil.getImageName("mariadb.10.image");
    private static final String MARIA_DB_VERSION = DbUtil.getImageVersion("mariadb.10.image");

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", "${mariadb.10.image}")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(MARIA_DB_NAME, MARIA_DB_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutMariadbDevServicePulling() {
        app.logs().assertContains(String.format("Pulling docker image: %s:%s", MARIA_DB_NAME, MARIA_DB_VERSION));
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for MariaDB started");
    }

    @Test
    public void verifyMysqlImage() {
        Image image = DockerUtils.getImage(MARIA_DB_NAME, MARIA_DB_VERSION);
        Assertions.assertFalse(image.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MARIA_DB_NAME, MARIA_DB_VERSION));
    }

    @AfterAll
    //TODO workaround for podman 4.4.1 on rhel. Without it, *next* test (eg MariaDBDatabaseIT) fails with "broken pipe"
    public static void clear() {
        DockerUtils.removeImage(MARIA_DB_NAME, MARIA_DB_VERSION);
    }
}
