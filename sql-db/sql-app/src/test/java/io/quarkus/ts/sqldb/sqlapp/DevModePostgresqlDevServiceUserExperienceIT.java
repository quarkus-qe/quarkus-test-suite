package io.quarkus.ts.sqldb.sqlapp;

import static io.quarkus.ts.sqldb.sqlapp.DbUtil.getImageName;
import static io.quarkus.ts.sqldb.sqlapp.DbUtil.getImageVersion;

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
public class DevModePostgresqlDevServiceUserExperienceIT {

    // we use '-alpine' version as no other test is using it, which reduce changes that image will be already pulled
    // we verified removing of Docker image in Github CI works, but either (extremely unlikely) Docker is shared between
    // instances, or this test is started when previous PostgreSQL container is being terminated and operation sometimes
    // fails; for whatever reason, using Alpine version makes CI less flaky
    // TODO: we should revise above-mentioned comments in order to determine if we still need this workaround
    private static final String POSTGRESQL_VERSION = getImageVersion("postgresql.latest.image") + "-alpine";
    private static final String POSTGRES_NAME = getImageName("postgresql.latest.image");

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "postgresql")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", "${postgresql.latest.image}-alpine")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(POSTGRES_NAME, POSTGRESQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutPostgresqlDevServicePulling() {
        app.logs().assertContains(String.format("Pulling docker image: %s:%s", POSTGRES_NAME, POSTGRESQL_VERSION));
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for PostgreSQL started");
    }

    @Test
    public void verifyPostgresqlImage() {
        Image postgresImg = DockerUtils.getImage(POSTGRES_NAME, POSTGRESQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                POSTGRES_NAME, POSTGRESQL_VERSION));
    }
}
