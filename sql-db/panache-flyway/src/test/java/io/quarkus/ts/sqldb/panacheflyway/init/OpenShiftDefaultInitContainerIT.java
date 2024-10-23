package io.quarkus.ts.sqldb.panacheflyway.init;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2071")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "Same as https://github.com/quarkus-qe/quarkus-test-suite/issues/2071")
public class OpenShiftDefaultInitContainerIT {

    private final Path openShiftYaml = Paths.get("target/", this.getClass().getSimpleName(),
            "app/target/kubernetes/openshift.yml");
    private static final String CUSTOM_IMAGE = "quay.io/quarkusqeteam/wait:0.0.2";

    @Container(image = "${mysql.80.image}", port = 3306, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.openshift.init-task-defaults.wait-for-container.image", CUSTOM_IMAGE)
            .withProperty("quarkus.flyway.schemas", database.getDatabase());

    @Test
    void migrated() {
        String userList = app.given()
                .accept("application/hal+json")
                .when().get("/users/all?sort=name")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().getString("_embedded.user_list.name");
        assertEquals("[Alaba, Balaba]", userList);
    }

    @Test
    void yaml() {
        try (InputStream content = Files.newInputStream(openShiftYaml)) {
            Iterable<Object> objects = new Yaml().loadAll(content);
            String image = null;
            for (Object object : objects) {
                CustomYaml source = new CustomYaml(object);
                if ("Deployment".equals(source.getValue("kind"))) {
                    image = source.get("spec")
                            .get("template")
                            .get("spec")
                            .getFromArray("initContainers", 0)
                            .getValue("image");
                }
            }
            Assertions.assertEquals(CUSTOM_IMAGE, image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
