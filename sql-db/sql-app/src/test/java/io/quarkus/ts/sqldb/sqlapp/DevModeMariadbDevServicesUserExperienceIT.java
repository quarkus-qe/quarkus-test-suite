package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.SocketUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMariadbDevServicesUserExperienceIT {

    private static final String MARIA_DB_VERSION = "5.5.49";
    private static final String MARIA_DB_NAME = "mariadb";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", MARIA_DB_NAME + ":" + MARIA_DB_VERSION)
            .withProperty("quarkus.hibernate-orm.dialect", "org.hibernate.dialect.MariaDB102Dialect")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(MARIA_DB_NAME, MARIA_DB_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutMariadbDevServicePulling() {
        app.logs().assertContains("Pulling docker image: mariadb");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        // TODO https://github.com/quarkusio/quarkus/issues/19573
        //app.logs().assertContains("Dev Services for mariadb started");
    }

    @Test
    public void verifyMysqlImage() {
        Image postgresImg = DockerUtils.getImage(MARIA_DB_NAME, MARIA_DB_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MARIA_DB_NAME, MARIA_DB_VERSION));
    }
}
