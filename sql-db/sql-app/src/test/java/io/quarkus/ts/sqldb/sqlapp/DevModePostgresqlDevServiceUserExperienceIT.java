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
public class DevModePostgresqlDevServiceUserExperienceIT {

    private static final String POSTGRESQL_VERSION = "9.6.23";
    private static final String POSTGRES_NAME = "postgres";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "postgresql")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", POSTGRES_NAME + ":" + POSTGRESQL_VERSION)
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(POSTGRES_NAME, POSTGRESQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutPostgresqlDevServicePulling() {
        app.logs().assertContains("Pulling docker image: postgres");
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        // TODO https://github.com/quarkusio/quarkus/issues/19573
        //app.logs().assertContains("Dev Services for postgres started");
    }

    @Test
    public void verifyPostgresqlImage() {
        Image postgresImg = DockerUtils.getImage(POSTGRES_NAME, POSTGRESQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                        "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                POSTGRES_NAME, POSTGRESQL_VERSION));
    }
}
