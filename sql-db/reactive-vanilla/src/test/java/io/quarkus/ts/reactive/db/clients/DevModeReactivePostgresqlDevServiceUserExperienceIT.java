package io.quarkus.ts.reactive.db.clients;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/44196")
@Tag("QUARKUS-1080")
@QuarkusScenario
public class DevModeReactivePostgresqlDevServiceUserExperienceIT {

    // we use '-bullseye' version as no other test is using it, which mitigates the fact that sometimes
    // io.quarkus.test.utils.DockerUtils.removeImage doesn't work as expected
    // TODO: drop suffix when https://github.com/quarkus-qe/quarkus-test-suite/issues/1227 is fixed
    private static final String POSTGRESQL_VERSION = getImageVersion("postgresql.latest.image") + "-bullseye";
    private static final String POSTGRES_NAME = getImageName("postgresql.latest.image");

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.devservices.image-name", "${postgresql.latest.image}-bullseye")
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
