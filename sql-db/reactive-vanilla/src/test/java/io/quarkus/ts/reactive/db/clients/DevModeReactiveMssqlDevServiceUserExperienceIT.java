package io.quarkus.ts.reactive.db.clients;

import static io.quarkus.test.utils.ImageUtil.getImageName;
import static io.quarkus.test.utils.ImageUtil.getImageVersion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndJava17;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.DockerUtils;

@DisabledOnFipsAndJava17(reason = "https://github.com/quarkusio/quarkus/issues/40813")
@Tag("QUARKUS-1408")
@QuarkusScenario
public class DevModeReactiveMssqlDevServiceUserExperienceIT {
    private static final String MSSQL_VERSION = getImageVersion("mssql.image");
    private static final String MSSQL_NAME = getImageName("mssql.image");

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.mssql.devservices.image-name", "${mssql.image}")
            .withProperty("quarkus.datasource.mysql.devservices.enabled", "false")
            .withProperty("quarkus.datasource.devservices.enabled", "false")
            .onPreStart(s -> DockerUtils.removeImage(MSSQL_NAME, MSSQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutReactiveMssqlDevServicePulling() {
        app.logs().assertContains(String.format("Pulling docker image: %s:%s", MSSQL_NAME, MSSQL_VERSION));
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for Microsoft SQL Server started");
    }

    @Test
    public void verifyReactiveMssqlImage() {
        Image mssqlImg = DockerUtils.getImage(MSSQL_NAME, MSSQL_VERSION);
        Assertions.assertFalse(mssqlImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MSSQL_NAME, MSSQL_VERSION));
    }
}
