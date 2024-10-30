package io.quarkus.ts.infinispan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@QuarkusScenario
public class DevModeInfinispanIT {

    // TODO: drop this Aarch64 workaround when https://issues.redhat.com/browse/QUARKUS-5242 is fixed
    private static final boolean IS_AARCH64_PLATFORM = Boolean.getBoolean("ts.arm.missing.services.excludes");

    @DevModeQuarkusApplication()
    static RestService service = new RestService()
            .withProperties(() -> {
                if (IS_AARCH64_PLATFORM) {
                    return Map.of("quarkus.infinispan-client.devservices.image-name", "${datagrid.image}");
                }
                return Map.of();
            })
            .onPreStart(service -> {
                if (IS_AARCH64_PLATFORM) {
                    // override default Ryuk used by Infinispan as it doesn't work on Aarch64
                    RestService restService = (RestService) service;
                    var testContainersPropsPath = restService
                            .getServiceFolder()
                            .resolve("src")
                            .resolve("main")
                            .resolve("resources")
                            .resolve("testcontainers.properties");
                    FileUtils.copyContentTo("ryuk.container.image=testcontainers/ryuk:0.3.3" + System.lineSeparator(),
                            testContainersPropsPath);
                }
            });

    @Test
    void smoke() {
        String firstCache = service.given()
                .get("/first-counter/get-cache")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();
        String secondCache = service.given()
                .get("/second-counter/get-cache")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals(firstCache, secondCache);
    }
}
