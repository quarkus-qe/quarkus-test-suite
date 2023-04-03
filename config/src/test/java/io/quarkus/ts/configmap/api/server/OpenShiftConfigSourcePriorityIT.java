package io.quarkus.ts.configmap.api.server;

import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.CONFIGMAP;
import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.SECRET;
import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.applyConfig;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftConfigSourcePriorityIT {

    private static final String CONFIGMAP_YAML = CONFIGMAP + "-yaml";
    private static final String CONFIGMAP_YML = CONFIGMAP + "-yml";
    private static final String SECRET_YML = SECRET + "-yml";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("config-source-priority.properties")
            .onPreStart(OpenShiftConfigSourcePriorityIT::loadConfigSource);

    @Inject
    static OpenShiftClient openShiftClient;

    @Test
    public void testConfigSourcePriorities() {
        final Response response = app.given()
                .get("/hello/properties");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        final HelloConfigProperties configProperties = response.as(HelloConfigProperties.class);
        assertNotNull(configProperties);

        // ConfigMap has higher priority than application properties, and it's possible to create ConfigMap from application.yml.
        assertEquals(format("epilogue %s", CONFIGMAP_YML), configProperties.epilogue);

        // Last ConfigMap has higher priority than ConfigMaps defined earlier, and it's possible to create ConfigMap from application.yaml.
        assertEquals(format("preamble %s", CONFIGMAP_YAML), configProperties.preamble);

        // Last Secret has higher priority than Secrets/ConfigMaps defined earlier.
        assertEquals(format("message %s", SECRET_YML), configProperties.message.orElse(null));

        // Environment variable has the highest priority.
        assertEquals("side note environment variable", configProperties.sideNote);
    }

    private static void loadConfigSource(Service service) {
        // create Secrets and ConfigMaps
        applyConfig(CONFIGMAP_YAML, openShiftClient);
        applyConfig(CONFIGMAP_YML, openShiftClient);
        applyConfig(CONFIGMAP, openShiftClient);
        applyConfig(SECRET, openShiftClient);
        applyConfig(SECRET_YML, openShiftClient);
    }
}
