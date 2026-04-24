package io.quarkus.ts.sqldb.sqlapp;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6234")
@QuarkusScenario
public class DevModeComposeLocationsIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static DevModeQuarkusService dockerComposeDevservicesApp = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}")
            .onPreStart(service -> ((DevModeQuarkusService) service)
                    .copyFile("src/test/resources/postgresql-compose-devservices.yml", "docker-compose-devservices.yml"));

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static DevModeQuarkusService dockerComposeDevserviceApp = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}")
            .setAutoStart(false)
            .onPreStart(service -> ((DevModeQuarkusService) service)
                    .copyFile("src/test/resources/postgresql-compose-devservices.yml", "docker-compose-devservice.yml"));

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static DevModeQuarkusService composeDevserviceApp = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}")
            .setAutoStart(false)
            .onPreStart(service -> ((DevModeQuarkusService) service)
                    .copyFile("src/test/resources/postgresql-compose-devservices.yml", "compose-devservice.yml"));

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static DevModeQuarkusService dockerComposeDevserviceYamlApp = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}")
            .setAutoStart(false)
            .onPreStart(service -> ((DevModeQuarkusService) service)
                    .copyFile("src/test/resources/postgresql-compose-devservices.yml", "docker-compose-devservice.yaml"));

    @DevModeQuarkusApplication(properties = "postgresql.properties")
    static DevModeQuarkusService suffixedDockerComposeDevserviceApp = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.compose.devservices.env-variables.IMAGE", "${postgresql.latest.image}")
            .setAutoStart(false)
            .onPreStart(service -> ((DevModeQuarkusService) service)
                    .copyFile("src/test/resources/postgresql-compose-devservices.yml", "docker-compose-devservice-foo.yml"));

    @Test
    public void composeDevServicesAreReused() {
        startStopDevserviceApps(List.of(dockerComposeDevserviceApp, composeDevserviceApp), true);

        // Compose dev service is triggered
        dockerComposeDevservicesApp.logs().assertContains("Compose is running command");
        dockerComposeDevservicesApp.logs().assertContains(System.getProperty("postgresql.latest.image"));

        // Compose dev service is reused (same name is defined in compose file) to avoid DB per application
        dockerComposeDevserviceApp.logs().assertContains("Compose Dev Service container found");
        dockerComposeDevserviceApp.logs().assertContains(System.getProperty("postgresql.latest.image"));

        composeDevserviceApp.logs().assertContains("Compose Dev Service container found");
        composeDevserviceApp.logs().assertContains(System.getProperty("postgresql.latest.image"));

        startStopDevserviceApps(List.of(dockerComposeDevserviceApp, composeDevserviceApp), false);
    }

    @Test
    public void composeDevServicesAreUsed() {
        startStopDevserviceApps(List.of(dockerComposeDevserviceYamlApp, suffixedDockerComposeDevserviceApp), true);

        dockerComposeDevserviceYamlApp.logs().assertContains("Compose Dev Service container found");
        dockerComposeDevserviceYamlApp.logs().assertContains(System.getProperty("postgresql.latest.image"));

        suffixedDockerComposeDevserviceApp.logs().assertContains("Compose Dev Service container found");
        suffixedDockerComposeDevserviceApp.logs().assertContains(System.getProperty("postgresql.latest.image"));

        startStopDevserviceApps(List.of(dockerComposeDevserviceYamlApp, suffixedDockerComposeDevserviceApp), false);
    }

    private void startStopDevserviceApps(List<DevModeQuarkusService> services, boolean start) {
        if (start) {
            services.forEach(DevModeQuarkusService::start);
        } else {
            services.forEach(DevModeQuarkusService::stop);
        }
    }
}
