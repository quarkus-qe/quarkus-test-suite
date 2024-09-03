package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.QuarkusDependency;
import static io.quarkus.test.util.QuarkusCLIUtils.QuarkusPlugin;
import static io.quarkus.test.util.QuarkusCLIUtils.addPluginsToPom;
import static io.quarkus.test.util.QuarkusCLIUtils.checkRenamesInFile;
import static io.quarkus.test.util.QuarkusCLIUtils.getPlugins;
import static io.quarkus.test.util.QuarkusCLIUtils.getPom;
import static io.quarkus.test.util.QuarkusCLIUtils.getProperties;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.util.QuarkusCLIUtils;

/**
 * Check updates from Quarkus 3.2 to 3.8
 */
@Tag("quarkus-cli")
@EnabledOnQuarkusVersion(version = "3.8.*redhat.*", reason = "This class is testing only updates to 3.8.* RHBQ versions")
@DisabledOnQuarkusVersion(version = "3.8.[0-5].*", reason = "https://github.com/quarkusio/quarkus/issues/42567")
public class Quarkus32to38CliUpdateIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldLtsStream = new DefaultArtifactVersion("3.2");
    private static final DefaultArtifactVersion newLtsStream = new DefaultArtifactVersion("3.8");
    private static final Path RENAME_METHOD_APP = Paths.get("src/test/resources/quarkus32apps/renameMethodApp");
    private static final Path RENAME_PACKAGE_APP = Paths.get("src/test/resources/quarkus32apps/renamePackageApp");
    private static final Path MULTI_MODULE_APP = Paths.get("src/test/resources/quarkus32apps/multiModuleApp");
    private static final Path RESTEASY_APP = Paths.get("src/test/resources/quarkus32apps/resteasyApp");
    private static final Path HIBERNATE_APP = Paths.get("src/test/resources/quarkus32apps/hibernate-search");

    public Quarkus32to38CliUpdateIT() {
        super(oldLtsStream, newLtsStream);
    }

    @Test
    public void propertiesUpdateTest() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.hibernate-search-orm.automatic-indexing.synchronization.strategy", "sync");
        oldProperties.put("quarkus.hibernate-search-orm.quarkusQE.automatic-indexing.synchronization.strategy", "sync");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.hibernate-search-orm.indexing.plan.synchronization.strategy", "sync");
        expectedNewProperties.put("quarkus.hibernate-search-orm.quarkusQE.indexing.plan.synchronization.strategy", "sync");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    @Test
    public void propertiesYamlUpdateTest() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.hibernate-search-orm.automatic-indexing.synchronization.strategy", "sync");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.hibernate-search-orm.indexing.plan.synchronization.strategy", "sync");

        QuarkusCLIUtils.checkYamlPropertiesUpdate(quarkusCLIAppManager, oldProperties, expectedNewProperties);
    }

    @Test
    public void dependencyUpdateTest() throws IOException, XmlPullParserException {
        List<Dependency> oldDependencies = new ArrayList<>();
        // Quarkus 3.3
        oldDependencies.add(new QuarkusDependency("org.graalvm.nativeimage:svm:24.0.1"));
        // Quarkus 3.6
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-jaeger:3:12.2"));
        // Disabled because of https://github.com/quarkusio/quarkus/issues/42201
        // oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-smallrye-opentracing:3.12.3"));
        // Quarkus 3.7
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-hibernate-search-orm-coordination-outbox-polling"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-rest-client"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-rest-client-jackson"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-rest-client-jaxb"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-rest-client-jsonb"));
        oldDependencies.add(new QuarkusDependency("io.quarkus:quarkus-rest-client-mutiny"));
        // these two should be removed without replacement
        oldDependencies.add(new QuarkusDependency("org.hibernate:hibernate-jpamodelgen:6.5.2.Final"));
        oldDependencies.add(new QuarkusDependency("org.hibernate.orm:hibernate-jpamodelgen:6.5.2.Final"));
        // Quarkus 3.8
        oldDependencies.add(new QuarkusDependency("org.graalvm.sdk:graal-sdk:24.0.2"));

        List<Dependency> newDependencies = new ArrayList<>();
        // Quarkus 3.3
        newDependencies.add(new QuarkusDependency("org.graalvm.sdk:graal-sdk:24.0.1"));
        // Quarkus 3.6
        newDependencies.add(new QuarkusDependency("io.quarkiverse.jaeger:quarkus-jaeger:1.0.0"));
        // Disabled because of https://github.com/quarkusio/quarkus/issues/42201
        // newDependencies.add(new QuarkusDependency("io.quarkiverse.opentracing:quarkus-smallrye-opentracing:1.0.0"));
        // Quarkus 3.7
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-hibernate-search-orm-outbox-polling"));
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-resteasy-client"));
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-resteasy-client-jackson"));
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-resteasy-client-jaxb"));
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-resteasy-client-jsonb"));
        newDependencies.add(new QuarkusDependency("io.quarkus:quarkus-resteasy-client-mutiny"));
        // Quarkus 3.8
        newDependencies.add(new QuarkusDependency("org.graalvm.sdk:nativeimage:24.0.2"));

        QuarkusCLIUtils.checkDependenciesUpdate(quarkusCLIAppManager, oldDependencies, newDependencies);
    }

    @Test
    public void pluginUpdateTest() throws XmlPullParserException, IOException {
        List<Plugin> oldPlugins = new ArrayList<>();
        // Quarkus 3.7
        oldPlugins.add(new QuarkusPlugin("org.apache.maven.plugins:maven-compiler-plugin:3.10.0"));
        oldPlugins.add(new QuarkusPlugin("org.apache.maven.plugins:maven-surefire-plugin:3.1.0"));

        List<Plugin> newPlugins = new ArrayList<>();
        // Quarkus 3.7
        newPlugins.add(new QuarkusPlugin("org.apache.maven.plugins:maven-compiler-plugin:3.12.1"));
        newPlugins.add(new QuarkusPlugin("org.apache.maven.plugins:maven-surefire-plugin:3.2.3"));

        QuarkusCLIUtils.checkPluginUpdate(quarkusCLIAppManager, oldPlugins, newPlugins);
    }

    /**
     * Updating maven-checkstyle is in separate test because it has version update,
     * but target version is not fixed, it only specifies 3.x - and this does not fit in our framework
     */
    @Test
    public void updateMavenCheckstylePluginTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();
        addPluginsToPom(app, List.of(new QuarkusPlugin("org.apache.maven.plugins:maven-checkstyle-plugin:2.17")));

        quarkusCLIAppManager.updateApp(app);

        Optional<Plugin> updatedCheckstylePlugin = getPlugins(app).stream()
                .filter(plugin -> plugin.getGroupId().equals("org.apache.maven.plugins") &&
                        plugin.getArtifactId().equals("maven-checkstyle-plugin"))
                .findFirst();

        assertTrue(updatedCheckstylePlugin.isPresent(),
                "org.apache.maven.plugins:maven-checkstyle-plugin should be present after update");
        assertTrue(updatedCheckstylePlugin.get().getVersion().startsWith("3."),
                "org.apache.maven.plugins:maven-checkstyle-plugin should be in version 3.x");
    }

    /**
     * Quarkus 3.7 update script enforces update java to 17 version
     */
    @Test
    public void javaUpdateTest() throws XmlPullParserException, IOException {
        // create app with java 11
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtraArgs("--java=11");
        assertEquals("11", getProperties(app).getProperty("maven.compiler.release"), "Java version should be 11 before update");

        quarkusCLIAppManager.updateApp(app);

        assertEquals("17", getProperties(app).getProperty("maven.compiler.release"), "Java version should be 17 after update");
    }

    @Test
    public void methodNameChangeTest() throws IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, RENAME_METHOD_APP);
        quarkusCLIAppManager.updateApp(app);

        Map<String, String> renames = new HashMap<>();
        // Quarkus 3.5
        renames.put("uniMemoize.atLeast", "uniMemoize.forFixedDuration");
        renames.put("uniAndGroup.combinedWith", "uniAndGroup.with");

        // Quarkus 3.7
        renames.put("logRecord.getThreadID", "logRecord.getLongThreadID");
        renames.put("logRecord.setThreadID", "logRecord.setLongThreadID");

        checkRenamesInFile(app.getFileFromApplication("src/main/java/org/acme", "MethodRenameResource.java"), renames);

        app.start();

        assertEquals("Hello Uni", app.given().get("/rename/uni/memoize").body().asString());
        assertEquals("Hello group", app.given().get("/rename/uni/group").body().asString());
        assertEquals("42", app.given().get("/rename/thread").body().asString());
    }

    @Test
    public void packageNameChangeTest() throws IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, RENAME_PACKAGE_APP);
        quarkusCLIAppManager.updateApp(app);

        Map<String, String> renames = new HashMap<>();
        // Quarkus 3.5
        renames.put("org.hibernate.search.mapper.orm.coordination.outboxpolling.OutboxPollingExtension",
                "org.hibernate.search.mapper.orm.outboxpolling.OutboxPollingExtension");
        renames.put("javax.security.cert.Certificate", "java.security.cert.Certificate");

        checkRenamesInFile(app.getFileFromApplication("src/main/java/org/acme", "PackageChangeResource.java"), renames);

        app.start();

        assertEquals("Hello cert", app.given().get("/rename/cert").body().asString());
        assertEquals("OutboxPollingExtension", app.given().get("/rename/extension").body().asString());
    }

    @Test
    public void updateMultiModuleAppTest() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, MULTI_MODULE_APP);
        quarkusCLIAppManager.updateApp(app);

        // parent
        assertTrue(getPom(app).getProperties().getProperty("quarkus.platform.version").startsWith("3.8"),
                "Parent project should be updated to quarkus 3.8.x");

        // rest-client (module)
        Dependency restClientDependency = new QuarkusDependency("io.quarkus:quarkus-resteasy-client-jackson");
        assertTrue(getPom(app, "rest-client").getDependencies().contains(restClientDependency),
                "quarkus-rest-client-jackson should be updated to quarkus-resteasy-client-jackson in child project");

        // TODO: it would be good to actually run the updated app, but FW currently cannot run multi module app
    }

    /**
     * Update resteasy app, while also upgrading it from java 11 to 17
     */
    @Test
    public void updateAndRunApp() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("app", null, RESTEASY_APP);
        assertEquals("11", getProperties(app).getProperty("maven.compiler.release"), "Java version should be 11 before update");

        quarkusCLIAppManager.updateApp(app);

        assertEquals("17", getProperties(app).getProperty("maven.compiler.release"), "Java version should be 17 after update");

        app.start();

        app.given().get("/book").then().statusCode(HttpStatus.SC_OK);
    }

    /**
     * Test updating hibernate app, which has properties changed in Quarkus 3.3
     */
    @Test
    public void hibernateUpdateTest() {
        QuarkusCliRestService app = cliClient.createApplicationFromExistingSources("hibernate-search", null, HIBERNATE_APP);

        quarkusCLIAppManager.updateApp(app);
        app.start();

        // search for entity
        app.given()
                .get("/library/author/search?pattern=John")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("firstName", contains("John"),
                        "lastName", contains("Irving"));
    }
}
