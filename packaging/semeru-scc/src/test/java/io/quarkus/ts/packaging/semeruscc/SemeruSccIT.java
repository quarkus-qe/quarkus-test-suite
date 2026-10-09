package io.quarkus.ts.packaging.semeruscc;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.junit.common.DisabledOnSemeru;
import io.quarkus.test.junit.common.EnabledOnSemeru;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Verifies the IBM Semeru OpenJ9 Shared Classes Cache (SCC) integration with Quarkus AOT packaging.
 */
@Tag("QUARKUS-7818")
@QuarkusScenario
@DisabledOnNative(reason = "SCC is a JVM-only feature; native mode is not applicable")
class SemeruSccIT {
    private static final String SCC_CACHE_DIR = "app-scc";
    private static final String XSHARECLASSES_KEY = "-Xshareclasses";

    /**
     * Scenario 1: SCC auto-detection on Semeru.
     */
    @QuarkusApplication
    static final RestService autoDetect = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            // Remove once https://github.com/quarkusio/quarkus/pull/57062 is available
            .withProperty("quarkus.package.jar.aot.phase", "build")
            .setAutoStart(false);

    /**
     * Scenario 2: Explicit SCC type override.
     */
    @QuarkusApplication
    static final RestService explicitScc = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            // Remove once https://github.com/quarkusio/quarkus/pull/57062 is available
            .withProperty("quarkus.package.jar.aot.phase", "build")
            .withProperty("quarkus.package.jar.aot.type", "SCC")
            .setAutoStart(false);

    // Scenario 3 (integration-test training phase). Check SemeruSccIntegrationTestPhaseIT

    /**
     * Scenario 4: Non-Semeru.
     */
    @QuarkusApplication
    static final RestService nonSemeruAutoDetect = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true");

    /**
     * Scenario 5: Missing cache graceful handling.
     */
    @QuarkusApplication
    static final RestService missingCacheFallback = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            // Remove once https://github.com/quarkusio/quarkus/pull/57062 is available
            .withProperty("quarkus.package.jar.aot.phase", "build")
            .setAutoStart(false);

    @Test
    @EnabledOnSemeru(reason = "SCC auto-detection only runs on Semeru JVMs")
    void sccAutoDetectedOnSemeru() {
        Path sccDir = sccDirectory(autoDetect);
        assertTrue(Files.isDirectory(sccDir),
                "app-scc/ directory must be created by the Quarkus build on Semeru: " + sccDir);

        startWithReadonlyScc(autoDetect);
        verifyEndpoints(autoDetect);
    }

    @Test
    @EnabledOnSemeru(reason = "SCC type override only applies on Semeru JVMs")
    void explicitSccTypeProducesSccDirectory() {
        Path sccDir = sccDirectory(explicitScc);
        assertTrue(Files.isDirectory(sccDir),
                "app-scc/ directory must be created when quarkus.package.jar.aot.type=SCC: " + sccDir);

        startWithReadonlyScc(explicitScc);
        verifyEndpoints(explicitScc);
    }

    @Test
    @DisabledOnSemeru(reason = "This test verifies that non-Semeru JVMs do not produce an app-scc/ directory")
    void noSccDirectoryOnNonSemeru() {
        Path sccDir = sccDirectory(nonSemeruAutoDetect);
        assertFalse(Files.isDirectory(sccDir),
                "app-scc/ directory must NOT be created on a non-Semeru JVM: " + sccDir);

        verifyEndpoints(nonSemeruAutoDetect);
    }

    @Test
    @Disabled("""
            Tested with both readonly and readonly,nonfatal against a missing cacheDir — both fail with JVMSHRC226E \
            Error opening shared class cache file / No such file or directory. The OpenJ9 docs explain why: nonfatal \
            covers cache initialization failures of an existing cache, not a missing directory. Additionally, the \
            readonly suboption is documented as ignored when AOT compilation is enabled, which is the case here  \
            (quarkus.package.jar.aot.enabled=true), so the JVM falls back to the default fatal behaviour regardless. \
            There is no Quarkus-level workaround for this; it is an OpenJ9 constraint.""")
    @EnabledOnSemeru(reason = "SCC fallback behaviour is an OpenJ9-specific feature")
    void missingCacheIsTolerantOnReadonly() throws IOException {
        Path sccDir = sccDirectory(missingCacheFallback);
        assertTrue(Files.isDirectory(sccDir),
                "app-scc/ directory must exist before deletion: " + sccDir);

        // Delete the SCC directory to simulate a missing cache
        deleteDirectory(sccDir);
        assertFalse(Files.exists(sccDir), "app-scc/ directory must be deleted before starting with readonly flag");

        // OpenJ9 silently falls back when the named cache is absent in readonly mode
        startWithReadonlyScc(missingCacheFallback);
        verifyEndpoints(missingCacheFallback);
    }

    private static Path sccDirectory(RestService app) {
        return app.getServiceFolder()
                .resolve("mvn-build")
                .resolve("target")
                .resolve("quarkus-app")
                .resolve(SCC_CACHE_DIR);
    }

    private static void startWithReadonlyScc(RestService app) {
        String absoluteSccDir = sccDirectory(app).toAbsolutePath().toString();
        app.withProperty(XSHARECLASSES_KEY, "name=quarkus-app,cacheDir=" + absoluteSccDir + ",readonly");
        app.start();
    }

    private static void verifyEndpoints(RestService app) {
        app.given()
                .get("/hello")
                .then()
                .statusCode(SC_OK);

        app.given()
                .get("/fruits")
                .then()
                .statusCode(SC_OK)
                .body("name", hasItems("Apple", "Banana", "Cherry"));
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }
}
