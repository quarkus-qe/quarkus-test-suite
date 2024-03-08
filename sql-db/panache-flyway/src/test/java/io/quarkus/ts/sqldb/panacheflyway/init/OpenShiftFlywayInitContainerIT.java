package io.quarkus.ts.sqldb.panacheflyway.init;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@Disabled("https://github.com/quarkusio/quarkus/issues/39230")
public class OpenShiftFlywayInitContainerIT {

    private final Path openShiftYaml = Paths.get("target/", this.getClass().getSimpleName(),
            "app/target/kubernetes/openshift.yml");
    private static final String CUSTOM_IMAGE = "quay.io/quarkusqeteam/wait";

    @Container(image = "${mysql.80.image}", port = 3306, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.openshift.init-containers.wait-for-flyway.image", CUSTOM_IMAGE)
            .withProperty("quarkus.openshift.init-containers.wait-for-flyway.image-pull-policy", "Always")
            .withProperty("quarkus.flyway.schemas", database.getDatabase());

    @Test
    @Disabled
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
            CustomYaml initContainer = null;
            for (Object object : objects) {
                CustomYaml source = new CustomYaml(object);
                if ("Deployment".equals(source.getValue("kind"))) {
                    initContainer = source.get("spec")
                            .get("template")
                            .get("spec")
                            .getFromArray("initContainers", 0);
                }
            }
            Assertions.assertNotNull(initContainer);
            Assertions.assertEquals(CUSTOM_IMAGE, initContainer.getValue("image"));
            Assertions.assertEquals("Always", initContainer.getValue("imagePullPolicy"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
