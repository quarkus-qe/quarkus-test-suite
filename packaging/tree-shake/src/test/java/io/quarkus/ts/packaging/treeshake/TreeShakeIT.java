package io.quarkus.ts.packaging.treeshake;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Verifies that a multi-extension application still works when tree-shaking removes unreachable
 * dependency classes. Every tree-shakeable JAR type is built both with {@code tree-shake.mode=classes}
 * and with {@code none}, so each shaken build is compared against its own same-type baseline.
 * Tree-shaking is a JVM packaging feature, so the whole scenario is disabled on native.
 */
@QuarkusScenario
@DisabledOnNative(reason = "Tree-shaking applies to JAR packaging; native builds do their own dead-code elimination")
class TreeShakeIT {

    private static final Logger LOG = Logger.getLogger(TreeShakeIT.class);

    @QuarkusApplication
    static final RestService fastJarShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "fast-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes");

    @QuarkusApplication
    static final RestService fastJarNotShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "fast-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "none");

    @QuarkusApplication
    static final RestService uberJarShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "uber-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes");

    @QuarkusApplication
    static final RestService uberJarNotShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "uber-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "none");

    @QuarkusApplication
    static final RestService legacyJarShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "legacy-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes");

    @QuarkusApplication
    static final RestService legacyJarNotShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "legacy-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "none");

    @QuarkusApplication
    static final RestService aotJarShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "aot-jar")
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes");

    @QuarkusApplication
    static final RestService aotJarNotShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "aot-jar")
            .withProperty("quarkus.package.jar.aot.enabled", "true")
            .withProperty("quarkus.package.jar.tree-shake.mode", "none");

    @QuarkusApplication
    static final RestService mutableJarShaken = new RestService()
            .withProperty("quarkus.package.jar.type", "mutable-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes");

    // excluded-artifacts is tree-shake config consumed by the same processor for every JAR type, so one
    // representative type (fast-jar) is enough to prove it
    @QuarkusApplication
    static final RestService fastJarExcluded = new RestService()
            .withProperty("quarkus.package.jar.type", "fast-jar")
            .withProperty("quarkus.package.jar.tree-shake.mode", "classes")
            .withProperty("quarkus.package.jar.tree-shake.excluded-artifacts", "com.h2database:h2");

    @Test
    void fastJar() throws IOException {
        verifyAllEndpoints(fastJarShaken);
        verifyAllEndpoints(fastJarNotShaken);
        verifyAllEndpoints(fastJarExcluded);
        assertShaken("fast-jar", fastJarShaken, fastJarNotShaken);
        assertExcludedArtifactPreserved("fast-jar", fastJarExcluded, fastJarNotShaken, fastJarShaken);
    }

    @Test
    void uberJar() throws IOException {
        verifyAllEndpoints(uberJarShaken);
        verifyAllEndpoints(uberJarNotShaken);
        assertShaken("uber-jar", uberJarShaken, uberJarNotShaken);
    }

    @Test
    void legacyJar() throws IOException {
        verifyAllEndpoints(legacyJarShaken);
        verifyAllEndpoints(legacyJarNotShaken);
        assertShaken("legacy-jar", legacyJarShaken, legacyJarNotShaken);
    }

    @Test
    void aotJar() throws IOException {
        verifyAllEndpoints(aotJarShaken);
        verifyAllEndpoints(aotJarNotShaken);
        assertShaken("aot-jar", aotJarShaken, aotJarNotShaken);
    }

    @Test
    void mutableJar() {
        // Tree-shaking is silently skipped for mutable-jar, so there is no size
        // reduction to assert. We only verify that requesting it does not break the build or the application.
        verifyAllEndpoints(mutableJarShaken);
    }

    /**
     * Verifies that a dependency listed in {@code tree-shake.excluded-artifacts} keeps all its classes.
     * The excluded build must retain the same number of h2 classes as the unshaken ({@code none}) build,
     * which ships the driver jar untouched, while a normally shaken build has fewer.
     */
    private static void assertExcludedArtifactPreserved(String jarType, RestService excluded, RestService notShaken,
            RestService shaken) throws IOException {
        long excludedCount = h2ClassCount(excluded);
        long notShakenCount = h2ClassCount(notShaken);
        long shakenCount = h2ClassCount(shaken);
        LOG.infof("Tree-shake %s h2 .class entries: excluded=%d, not-shaken baseline=%d, shaken=%d",
                jarType, excludedCount, notShakenCount, shakenCount);
        assertEquals(notShakenCount, excludedCount, "Excluded h2 in " + jarType + " should keep all classes: excluded ("
                + excludedCount + ") must match the not-shaken build (" + notShakenCount + ")");
        assertTrue(shakenCount < notShakenCount, "Without exclusion, tree-shaking should remove some h2 classes in "
                + jarType + " (shaken=" + shakenCount + ", notShaken=" + notShakenCount + ")");
    }

    /**
     * Counts the h2 driver {@code .class} entries (package {@code org/h2/}) in a fast-jar build's h2
     * dependency jar under {@code quarkus-app/lib}. Matching by package rather than by jar file name avoids
     * accidentally picking the {@code quarkus-jdbc-h2} extension jar instead of the driver.
     */
    private static long h2ClassCount(RestService app) throws IOException {
        Path lib = app.getServiceFolder().resolve("mvn-build").resolve("target").resolve("quarkus-app").resolve("lib");
        Path h2Jar;
        try (Stream<Path> jars = Files.walk(lib)) {
            h2Jar = jars.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("com.h2database.h2-"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No h2 driver jar found under " + lib));
        }
        try (ZipFile zip = new ZipFile(h2Jar.toFile())) {
            return zip.stream()
                    .filter(e -> !e.isDirectory())
                    .filter(e -> e.getName().endsWith(".class") && e.getName().contains("org/h2/"))
                    .count();
        }
    }

    /**
     * Asserts the shaken build of a given JAR type is smaller than the same-type {@code none} baseline.
     */
    private static void assertShaken(String jarType, RestService shaken, RestService notShaken) throws IOException {
        long shakenSize = buildOutputSize(shaken);
        long baselineSize = buildOutputSize(notShaken);
        LOG.infof("Tree-shake %s size: shaken=%d bytes, not-shaken baseline=%d bytes, reduction=%d bytes (%.1f%%)",
                jarType, shakenSize, baselineSize, baselineSize - shakenSize,
                100.0 * (baselineSize - shakenSize) / baselineSize);
        assertTrue(shakenSize < baselineSize, "Tree-shaken " + jarType + " (" + shakenSize
                + " bytes) should be smaller than the not-shaken baseline (" + baselineSize + " bytes)");
    }

    /**
     * Exercises data sources + Jackson, Qute, OpenAPI, JWT security and logging.
     * A class incorrectly removed by tree-shaking would surface here as a failure or a
     * ClassNotFoundException / NoClassDefFoundError at runtime.
     */
    private static void verifyAllEndpoints(RestService app) {
        // Data source (Hibernate ORM + H2) and REST with Jackson serialization
        app.given()
                .get("/fruits")
                .then()
                .statusCode(SC_OK)
                .body("name", hasItems("Apple", "Banana", "Cherry"));

        // Qute template rendering
        app.given()
                .get("/greeting?name=Quarkus")
                .then()
                .statusCode(SC_OK)
                .body(containsString("Hello Quarkus!"));

        // OpenAPI document generation
        app.given()
                .get("/q/openapi")
                .then()
                .statusCode(SC_OK)
                .body(containsString("Tree Shake API"));

        // Security: the role-protected endpoint must reject an unauthenticated request...
        app.given()
                .get("/secured")
                .then()
                .statusCode(SC_UNAUTHORIZED);

        // ...and grant access once a valid JWT is presented (JBoss Logging on the secured path)
        String token = app.given()
                .get("/token")
                .then()
                .statusCode(SC_OK)
                .extract().asString();

        app.given()
                .header("Authorization", "Bearer " + token)
                .get("/secured")
                .then()
                .statusCode(SC_OK)
                .body(containsString("tester@quarkus.io"));
    }

    /**
     * Total size of a service's build output. Tree-shaking removes unreachable classes from the
     * dependency jars, so a shaken build is smaller than the same application built with
     * {@code tree-shake.mode=none}. The layout differs per JAR type:
     * <ul>
     * <li>fast-jar / aot-jar / mutable-jar: a {@code quarkus-app} directory,</li>
     * <li>uber-jar: a single {@code *-runner.jar},</li>
     * <li>legacy-jar: a {@code *-runner.jar} plus a {@code lib} directory.</li>
     * </ul>
     */
    private static long buildOutputSize(RestService app) throws IOException {
        Path target = app.getServiceFolder().resolve("mvn-build").resolve("target");
        Path quarkusApp = target.resolve("quarkus-app");
        if (Files.isDirectory(quarkusApp)) {
            return dirSize(quarkusApp);
        }
        long size = 0;
        try (Stream<Path> files = Files.list(target)) {
            size += files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("-runner.jar"))
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        }
        Path lib = target.resolve("lib");
        if (Files.isDirectory(lib)) {
            size += dirSize(lib);
        }
        return size;
    }

    private static long dirSize(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(Files::isRegularFile).mapToLong(p -> p.toFile().length()).sum();
        }
    }
}
