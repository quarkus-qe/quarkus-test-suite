package io.quarkus.ts.sqldb.sqlapp;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;
import io.quarkus.test.utils.SocketUtils;

@QuarkusScenario
@Tag("podman-incompatible") //TODO: https://github.com/quarkusio/quarkus/issues/38003
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
public class DevModeOracleDevServiceUserExperienceIT {

    private static final String ORACLE_NAME = getImageName("oracle.image");
    private static final String ORACLE_VERSION = getImageVersion("oracle.image");

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "oracle")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", "${oracle.image}")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(ORACLE_NAME, ORACLE_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutOracleDevServicePulling() {
        app.logs().assertContains("Pulling docker image: " + ORACLE_NAME);
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for Oracle started");
    }

    @Test
    public void verifyOracleImage() {
        Image oracleImg = DockerUtils.getImage(ORACLE_NAME, ORACLE_VERSION);
        Assertions.assertFalse(oracleImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                ORACLE_NAME, ORACLE_VERSION));
    }

    @AfterAll
    //TODO workaround for podman 4.4.1 on rhel. Without it, *next* test (eg MariaDBDatabaseIT) fails with "broken pipe"
    public static void clear() {
        DockerUtils.removeImage(ORACLE_NAME, ORACLE_VERSION);
    }
}
