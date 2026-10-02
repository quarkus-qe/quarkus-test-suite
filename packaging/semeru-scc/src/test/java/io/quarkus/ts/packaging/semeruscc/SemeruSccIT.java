package io.quarkus.ts.packaging.semeruscc;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SemeruSccIT {
    private static final String SCC_CACHE_DIR = "app-scc";
    private static final String XSHARECLASSES_KEY = "-Xshareclasses";

    /**
     * Shared between the auto-detection and explicit-type Semeru tests below. Each reconfigures it via
     * {@code withProperty} and {@code restart()}, which only triggers a rebuild when a build-time property
     * actually changed, and only one instance of it is ever running at a time.
     * <p>
     * Not reused by {@link #missingCacheIsTolerantOnReadonly()}: {@code buildPropertiesChanged()} compares
     * against a snapshot taken once at the first build and never refreshed, so once a build-time property is
     * set here (quarkus.package.jar.aot.type in the test below), every later {@code restart()} keeps seeing it
     * as "changed" and keeps rebuilding - which would recreate the app-scc/ directory that
     * {@link #missingCacheIsTolerantOnReadonly()} deliberately deletes. Giving that scenario its own instance,
     * whose properties never change after its first build, avoids that framework quirk entirely.
     */
    @QuarkusApplication
    static final RestService semeruScc = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            // Remove once https://github.com/quarkusio/quarkus/pull/57062 is available
            .withProperty("quarkus.package.jar.aot.phase", "build")
            .setAutoStart(false);

    /**
     * Non-Semeru: AOT auto-detection must not produce an app-scc/ directory.
     */
    @QuarkusApplication
    static final RestService nonSemeruAutoDetect = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true");

    /**
     * Dedicated instance for {@link #missingCacheIsTolerantOnReadonly()}. See the note on {@link #semeruScc}
     * for why this scenario can't share that instance.
     */
    @QuarkusApplication
    static final RestService missingCacheFallback = new RestService()
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            // Remove once https://github.com/quarkusio/quarkus/pull/57062 is available
            .withProperty("quarkus.package.jar.aot.phase", "build")
            .setAutoStart(false);

    @Test
    @Order(1)
    @EnabledOnSemeru(reason = "SCC auto-detection only runs on Semeru JVMs")
    void sccAutoDetectedOnSemeru() {
        startWithReadonlyScc(semeruScc);

        assertTrue(Files.isDirectory(sccDirectory(semeruScc)),
                "app-scc/ directory must be created by the Quarkus build on Semeru: " + sccDirectory(semeruScc));
        verifyEndpoints(semeruScc);

        semeruScc.stop();
    }

    @Test
    @Order(2)
    @EnabledOnSemeru(reason = "SCC type override only applies on Semeru JVMs")
    void explicitSccTypeProducesSccDirectory() {
        semeruScc.withProperty("quarkus.package.jar.aot.type", "SCC");
        startWithReadonlyScc(semeruScc);

        assertTrue(Files.isDirectory(sccDirectory(semeruScc)),
                "app-scc/ directory must be created when quarkus.package.jar.aot.type=SCC: " + sccDirectory(semeruScc));
        verifyEndpoints(semeruScc);

        semeruScc.stop();
    }

    @Test
    @Order(3)
    @DisabledOnSemeru(reason = "This test verifies that non-Semeru JVMs do not produce an app-scc/ directory")
    void noSccDirectoryOnNonSemeru() {
        Path sccDir = sccDirectory(nonSemeruAutoDetect);
        assertFalse(Files.isDirectory(sccDir),
                "app-scc/ directory must NOT be created on a non-Semeru JVM: " + sccDir);

        verifyEndpoints(nonSemeruAutoDetect);
    }

    @Test
    @Order(4)
    @EnabledOnSemeru(reason = "SCC fallback behaviour is an OpenJ9-specific feature")
    void missingCacheIsTolerantOnReadonly() throws IOException {
        Path sccDir = sccDirectory(missingCacheFallback);
        assertTrue(Files.isDirectory(sccDir),
                "app-scc/ directory must exist before deletion: " + sccDir);

        // Delete the SCC directory to simulate a missing cache
        deleteDirectory(sccDir);
        assertFalse(Files.exists(sccDir), "app-scc/ directory must be deleted before starting with readonly flag");

        assertThrows(Throwable.class, () -> startWithReadonlyScc(missingCacheFallback));
        assertTrue(missingCacheFallback.getLogs().contains("JVMSHRC226E Error opening shared class cache file"));

        missingCacheFallback.stop();
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
        app.restartAndWaitUntilServiceIsStarted();
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
