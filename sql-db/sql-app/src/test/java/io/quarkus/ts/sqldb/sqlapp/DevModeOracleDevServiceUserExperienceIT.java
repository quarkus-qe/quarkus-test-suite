package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;
import io.quarkus.test.utils.SocketUtils;

@QuarkusScenario
public class DevModeOracleDevServiceUserExperienceIT {

    private static final String ORACLE_VERSION = "18.4.0-slim";
    private static final String ORACLE_NAME = "gvenzl/oracle-xe";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "oracle")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", ORACLE_NAME + ":" + ORACLE_VERSION)
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
}
