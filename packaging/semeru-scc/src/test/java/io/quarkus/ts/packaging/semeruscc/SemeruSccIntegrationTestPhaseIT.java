package io.quarkus.ts.packaging.semeruscc;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.common.EnabledOnSemeru;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

/**
 * Scenario 3: the integration-test training phase.
 */
@Tag("QUARKUS-7818")
@QuarkusIntegrationTest
@EnabledOnSemeru(reason = "Integration-test training phase only produces an SCC on Semeru JVMs")
@DisabledOnNative(reason = "SCC is a JVM-only feature; native mode is not applicable")
class SemeruSccIntegrationTestPhaseIT {

    private static final Path SCC_CACHE_DIR = Path.of("target", "quarkus-app", "app-scc");

    @Test
    void sccIsTrainedWhileIntegrationTestsRun() throws IOException {
        given()
                .get("/hello")
                .then()
                .statusCode(SC_OK);

        given()
                .get("/fruits")
                .then()
                .statusCode(SC_OK)
                .body("name", hasItems("Apple", "Banana", "Cherry"));

        assertTrue(Files.isDirectory(SCC_CACHE_DIR),
                "app-scc/ must be created by the integration-test training phase: " + SCC_CACHE_DIR.toAbsolutePath());
        assertTrue(cacheFileCount() > 0,
                "app-scc/ must contain at least one shared class cache file: " + SCC_CACHE_DIR.toAbsolutePath());
    }

    private static long cacheFileCount() throws IOException {
        try (Stream<Path> files = Files.list(SCC_CACHE_DIR)) {
            return files.filter(Files::isRegularFile).count();
        }
    }
}
