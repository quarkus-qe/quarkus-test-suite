package io.quarkus.ts.reactive.db.clients;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

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

    // we use '-bullseye' version as no other test is using it, which mitigates the fact that sometimes
    // io.quarkus.test.utils.DockerUtils.removeImage doesn't work as expected
    // TODO: drop suffix when https://github.com/quarkus-qe/quarkus-test-suite/issues/1227 is fixed
    private static final String POSTGRESQL_VERSION;
    private static final String POSTGRES_NAME;
    private static final String POSTGRESQL_IMAGE_NAME;

    static {
        // TODO: simplify when https://github.com/quarkus-qe/quarkus-test-suite/pull/2147 is fixed
        boolean useDevSvcSpecificImg = System.getProperty("postgresql.dev.svc.latest.image") != null;
        if (useDevSvcSpecificImg) {
            POSTGRESQL_IMAGE_NAME = "postgresql.dev.svc.latest.image";
        } else {
            POSTGRESQL_IMAGE_NAME = "postgresql.latest.image";
        }
        POSTGRESQL_VERSION = getImageVersion(POSTGRESQL_IMAGE_NAME) + "-bullseye";
        POSTGRES_NAME = getImageName(POSTGRESQL_IMAGE_NAME);
    }

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.devservices.image-name", "${" + POSTGRESQL_IMAGE_NAME + "}-bullseye")
            .withProperty("quarkus.datasource.mysql.devservices.enabled", "false")
            .withProperty("quarkus.datasource.mariadb.devservices.enabled", "false")
            .withProperty("quarkus.datasource.mssql.devservices.enabled", "false")
            .onPreStart(s -> DockerUtils.removeImage(POSTGRES_NAME, POSTGRESQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutReactivePostgresqlDevServicePulling() {
        app.logs().assertContains(String.format("Pulling docker image: %s:%s", POSTGRES_NAME, POSTGRESQL_VERSION));
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
