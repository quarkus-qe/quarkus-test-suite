package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.QuarkusDependency;
import static io.quarkus.test.util.QuarkusCLIUtils.addDependenciesToPom;
import static io.quarkus.test.util.QuarkusCLIUtils.getDependencies;
import static io.quarkus.test.util.QuarkusCLIUtils.getPom;
import static io.quarkus.test.util.QuarkusCLIUtils.readPropertiesFile;
import static io.quarkus.test.util.QuarkusCLIUtils.writePropertiesToPropertiesFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.surefire.shared.lang3.tuple.Pair;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.util.QuarkusCLIUtils;

@Tag("quarkus-cli")
public class QuarkusCli38to315UpdateIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldLtsStream = new DefaultArtifactVersion("3.8");
    private static final DefaultArtifactVersion newLtsStream = new DefaultArtifactVersion("3.15");
    private static final String OLD_COMMUNITY_VERSION = "3.8.5";

    private static final Path DEPENDENCY_UPDATE_CSV = Paths.get("src/test/resources/quarkus315dependencyUpdates.csv");
    private static final Path RESTEASY_REACTIVE_APP = Paths.get("src/test/resources/quarkus38apps/resteasyReactiveApp");
    private static final Path MESSAGING_APP = Paths.get("src/test/resources/quarkus38apps/messagingAmqp");
    private static final Path HIBERNATE_APP = Paths.get("src/test/resources/quarkus38apps/hibernate");
    private static final Path MULTI_MODULE_APP = Paths.get("src/test/resources/quarkus38apps/multiModuleApp");

    public QuarkusCli38to315UpdateIT() {
        super(oldLtsStream, newLtsStream);
    }

    /**
     * Validate that dependencies are changed in a way that is documented in migration guides for quarkus versions 3.9 - 3.15.
     */
    @Test
    public void dependencyUpdateTest() throws IOException, XmlPullParserException {
        List<Pair<Dependency, Dependency>> dependenciesToUpdate = loadDependencyPairsFromCSV(DEPENDENCY_UPDATE_CSV);
        checkDependenciesUpdate(quarkusCLIAppManager, dependenciesToUpdate);
    }

    /**
     * Validate properties are changed in way they are documented in 3.9 - 3.15.
     */
    @Test
    public void propertiesUpdateTest() throws IOException {
        Properties oldProperties = new Properties();
        Properties expectedNewProperties = new Properties();

        // quarkus 3.9
        // https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.9#resteasy-reactive-extensions-renamed-to-quarkus-rest-gear-white_check_mark
        oldProperties.put("quarkus.resteasy-reactive.path", "/api");
        expectedNewProperties.put("quarkus.rest.path", "/api");
        oldProperties.put("quarkus.rest-client-reactive.extensions-api.url", "https://stage.code.quarkus.io/api");
        expectedNewProperties.put("quarkus.rest-client.extensions-api.url", "https://stage.code.quarkus.io/api");
        oldProperties.put("quarkus.oidc-client-reactive-filter.enabled", "false");
        expectedNewProperties.put("quarkus.rest-client-oidc-filter.enabled", "false");
        oldProperties.put("quarkus.oidc-token-propagation-reactive.enabled", "true");
        expectedNewProperties.put("quarkus.rest-csrf.cookie-name", "csrfToken");
        oldProperties.put("quarkus.csrf-reactive.cookie-name", "csrfToken");
        expectedNewProperties.put("quarkus.rest-client-oidc-token-propagation.enabled", "true");
        oldProperties.put("quarkus.oidc-client-filter.enabled", "true");
        expectedNewProperties.put("quarkus.resteasy-client-oidc-filter.enabled", "true");
        oldProperties.put("quarkus.oidc-token-propagation.enabled", "true");
        expectedNewProperties.put("quarkus.resteasy-client-oidc-token-propagation.enabled", "true");

        // quarkus 3.10
        // https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
        oldProperties.put("quarkus.package.create-appcds", "false");
        expectedNewProperties.put("quarkus.package.jar.appcds.enabled", "false");
        oldProperties.put("quarkus.package.appcds-builder-image", "docker://dontknow");
        expectedNewProperties.put("quarkus.package.jar.appcds.builder-image", "docker://dontknow");
        oldProperties.put("quarkus.package.appcds-use-container", "false");
        expectedNewProperties.put("quarkus.package.jar.appcds.use-container", "false");
        oldProperties.put("quarkus.package.compress-jar", "false");
        expectedNewProperties.put("quarkus.package.jar.compress", "false");
        oldProperties.put("quarkus.package.filter-optional-dependencies", "false");
        expectedNewProperties.put("quarkus.package.jar.filter-optional-dependencies", "false");
        oldProperties.put("quarkus.package.add-runner-suffix", "false");
        expectedNewProperties.put("quarkus.package.jar.add-runner-suffix", "false");
        oldProperties.put("quarkus.package.user-configured-ignored-entries", "false");
        expectedNewProperties.put("quarkus.package.jar.user-configured-ignored-entries", "false");
        oldProperties.put("quarkus.package.user-providers-directory", "true");
        expectedNewProperties.put("quarkus.package.jar.user-providers-directory", "true");
        oldProperties.put("quarkus.package.included-optional-dependencies", "true");
        expectedNewProperties.put("quarkus.package.jar.included-optional-dependencies", "true");
        oldProperties.put("quarkus.package.include-dependency-list", "true");
        expectedNewProperties.put("quarkus.package.jar.include-dependency-list", "true");
        oldProperties.put("quarkus.package.decompiler.enabled", "true");
        oldProperties.put("quarkus.package.vineflower.enabled", "true");
        expectedNewProperties.put("quarkus.package.jar.decompiler.enabled", "true");
        oldProperties.put("quarkus.package.decompiler.jar-directory", "/tmp");
        oldProperties.put("quarkus.package.vineflower.jar-directory", "/tmp");
        expectedNewProperties.put("quarkus.package.jar.decompiler.jar-directory", "/tmp");
        oldProperties.put("quarkus.package.manifest.attributes.enabled", "true");
        expectedNewProperties.put("quarkus.package.jar.manifest.attributes.enabled", "true");
        oldProperties.put("quarkus.package.manifest.sections.enabled", "true");
        expectedNewProperties.put("quarkus.package.jar.manifest.sections.enabled", "true");
        oldProperties.put("quarkus.package.manifest.add-implementation-entries", "true");
        expectedNewProperties.put("quarkus.package.jar.manifest.add-implementation-entries", "true");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * In quarkus 3.10, some dependencies are added if certain conditions are met.
     * But in this case, no dependencies are removed, so cannot use .checkDependenciesUpdate().
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#flyway-10-gear-white_check_mark
     */
    @Test
    public void addDependenciesTest() throws XmlPullParserException, IOException {
        List<Dependency> oldDependencies = new ArrayList<>();
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-flyway"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-jdbc-derby"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-jdbc-db2"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-jdbc-postgresql"));

        List<Dependency> expectedDependencies = new ArrayList<>();
        expectedDependencies.add(new QuarkusDependency("org.flywaydb:flyway-database-derby"));
        expectedDependencies.add(new QuarkusDependency("org.flywaydb:flyway-database-db2"));
        expectedDependencies.add(new QuarkusDependency("org.flywaydb:flyway-database-postgresql"));

        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();
        addDependenciesToPom(app, oldDependencies);
        quarkusCLIAppManager.updateApp(app);

        List<Dependency> actualDependencies = getDependencies(app);
        expectedDependencies.forEach((dependency) -> Assertions.assertTrue(actualDependencies.contains(dependency),
                "Pom.xml after update should contain dependency: " + dependency));
    }

    /**
     * Quarkus 3.13 has changed, the way how they check if dev service should start.
     * OIDC is one that is automatically changed during update. Check it works.
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.13#dev-services-startup-detection-change
     */
    @Test
    public void quarkus313KeycloakRenameTest() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("%test.quarkus.oidc.auth-server-url", "${keycloak.url}/realms/quarkus/");

        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();
        writePropertiesToPropertiesFile(app, oldProperties);
        quarkusCLIAppManager.updateApp(app);

        Properties newProperties = readPropertiesFile(app);
        assertTrue(newProperties.containsKey("%test.quarkus.oidc.auth-server-url"), "Keycloak url should  be present");
        assertEquals("${keycloak.url:replaced-by-test-resource}/realms/quarkus/",
                newProperties.getProperty("%test.quarkus.oidc.auth-server-url"), "Keycloak url should be rewritten");
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=jar works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestJar() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "jar");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.type", "jar");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=fast-jar works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestFastJar() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "fast-jar");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.type", "fast-jar");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=uber-jar works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestUberJar() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "uber-jar");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.type", "uber-jar");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=mutable-jar works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestMutableJar() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "mutable-jar");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.type", "mutable-jar");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=native works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestNative() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "native");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.enabled", "false");
        expectedNewProperties.put("quarkus.native.enabled", "true");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus 3.10 changes way how property quarkus.package.type works.
     * Check that type=native-sources works properly
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.10#packaging-configuration-gear-white_check_mark
     */
    @Test
    public void quarkus310PackageTypeChangeTestNativeSources() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.package.type", "native-sources");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.package.jar.enabled", "false");
        expectedNewProperties.put("quarkus.native.enabled", "true");
        expectedNewProperties.put("quarkus.native.sources-only", "true");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    /**
     * Quarkus <a href="https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.9">3.9</a> renamed rest(easy) extensions.
     * Test that application with quarkus-resteasy-something dependency can be upgraded and work.
     */
    @Test
    public void updateResteasyReactiveAppTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, RESTEASY_REACTIVE_APP);
        QuarkusCLIUtils.setCommunityBomIfNotRunningRHBQ(app, OLD_COMMUNITY_VERSION);

        quarkusCLIAppManager.updateApp(app);

        List<Dependency> dependencies = getDependencies(app);
        assertFalse(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-resteasy-reactive")),
                "quarkus-resteasy-reactive should be renamed to quarkus-rest");
        assertFalse(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-rest-client-reactive-jackson")),
                "quarkus-rest-client-reactive-jackson should be renamed to quarkus-rest-client-jackson");

        assertTrue(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-rest")),
                "quarkus-resteasy-reactive should be renamed to quarkus-rest");
        assertTrue(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-rest-client-jackson")),
                "quarkus-rest-client-reactive-jackson should be renamed to quarkus-rest-client-jackson");

        app.start();
        app.given().get("/book").then().statusCode(HttpStatus.SC_OK);
    }

    /**
     * Quarkus <a href="https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.9">3.9</a> renamed messaging extensions.
     * Test that application with messaging dependency can be upgraded and work.
     */
    @Test
    public void updateMessagingAmqpAppTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, MESSAGING_APP);
        QuarkusCLIUtils.setCommunityBomIfNotRunningRHBQ(app, OLD_COMMUNITY_VERSION);

        quarkusCLIAppManager.updateApp(app);

        List<Dependency> dependencies = getDependencies(app);
        assertFalse(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-smallrye-reactive-messaging-amqp")),
                "quarkus-smallrye-reactive-messaging-amqp should be renamed to quarkus-messaging-amqp");

        assertTrue(dependencies.contains(new QuarkusDependency("io.quarkus:quarkus-messaging-amqp")),
                "quarkus-smallrye-reactive-messaging-amqp should be renamed to quarkus-messaging-amqp");

        app.start();
        assertTrue(app.given().get("/price").thenReturn().body().asString().contains("20"),
                "App should return not-empty result");
    }

    /**
     * Hibernate had several changes smaller between quarkus 3.9 and 3.15.
     * Especially Hibernate ORM changed from 6.4 up to 6.6.
     * Test that hibernate app will upgrade successfully and still work.
     * See
     * - https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.11#jpa--hibernate-orm
     * - https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.12#hibernate-orm
     * - https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.13#hibernate-orm
     * - https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.14#jpa-hibernate-orm
     */
    @Test
    public void updateHibernateAppTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, HIBERNATE_APP);
        QuarkusCLIUtils.setCommunityBomIfNotRunningRHBQ(app, OLD_COMMUNITY_VERSION);

        quarkusCLIAppManager.updateApp(app);

        app.start();
        assertTrue(app.given().get("/items/count").thenReturn().body().asString().contains("1"),
                "App should return not-empty result");
    }

    /**
     * Use an application with multiple modules and test it will be upgraded properly.
     */
    @Test
    public void updateMultiModuleAppTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, MULTI_MODULE_APP);
        QuarkusCLIUtils.setCommunityBomIfNotRunningRHBQ(app, OLD_COMMUNITY_VERSION);
        quarkusCLIAppManager.updateApp(app);

        // parent
        assertTrue(getPom(app).getProperties().getProperty("quarkus.platform.version").startsWith(newLtsStream.toString()),
                "Parent project should be updated to quarkus " + newLtsStream.toString() + ".x");

        Dependency restDependency = new QuarkusDependency("io.quarkus:quarkus-rest");
        // rest-client (child module)
        assertTrue(getPom(app, "rest-client").getDependencies().contains(restDependency),
                "quarkus-resteasy-reactive should be updated to quarkus-rest in rest-client child project");
        // rest-service (child module)
        assertTrue(getPom(app, "rest-service").getDependencies().contains(restDependency),
                "quarkus-resteasy-reactive should be updated to quarkus-rest in rest-service child project");

        // TODO: it would be good to actually run the updated app, but FW currently cannot run multi module app
    }
}
