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
public class DevModeMysqlDevServiceUserExperienceIT {

    private static final String MYSQL_VERSION = "5.7.32";
    private static final String MYSQL_NAME = "mysql";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mysql")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", MYSQL_NAME + ":" + MYSQL_VERSION)
            .withProperty("quarkus.hibernate-orm.dialect", "org.hibernate.dialect.MariaDB102Dialect")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(MYSQL_NAME, MYSQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutMysqlDevServicePulling() {
        app.logs().assertContains("Pulling docker image: mysql");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        // TODO https://github.com/quarkusio/quarkus/issues/19573
        //app.logs().assertContains("Dev Services for Mysql started");
    }

    @Test
    public void verifyMysqlImage() {
        Image postgresImg = DockerUtils.getImage(MYSQL_NAME, MYSQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MYSQL_NAME, MYSQL_VERSION));
    }
}
