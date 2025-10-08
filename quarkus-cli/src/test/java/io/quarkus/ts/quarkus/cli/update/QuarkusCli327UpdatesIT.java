package io.quarkus.ts.quarkus.cli.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.util.QuarkusCLIUtils;
import io.quarkus.test.utils.FileUtils;

/**
 * Tests Quarkus CLI update command recipes for changes between 3.20 and 3.27.
 */
@Tag("quarkus-cli")
public class QuarkusCli327UpdatesIT extends AbstractQuarkusCliUpdateIT {

    public QuarkusCli327UpdatesIT() {
        super(new DefaultArtifactVersion("3.20"), new DefaultArtifactVersion("3.27"));
    }

    /**
     * Tests Quarkus CLI update recipe for 3.21.
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.21#tls-registry-gear-white_check_mark">the
     * migration guide note</a>.
     */
    @Test
    void testTlsRegistryBuildItemPackageChange() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("tls-registry", "quarkus-rest");

        // we obviously don't have extension and the deployment module, but we just need to verify the update command
        Path orgAcmePackagePath = app.getServiceFolder().resolve("src").resolve("main").resolve("java").resolve("org")
                .resolve("acme");
        File testProcessorJavaFile = new File("src/test/resources/quarkus327/sources/tls-registry/TestProcessor.java");
        FileUtils.copyFileTo(testProcessorJavaFile, orgAcmePackagePath);

        var tlsRegistryDeployment = new Dependency();
        tlsRegistryDeployment.setGroupId("io.quarkus");
        tlsRegistryDeployment.setArtifactId("quarkus-tls-registry-deployment");
        QuarkusCLIUtils.addDependenciesToPom(app, List.of(tlsRegistryDeployment));

        quarkusCLIAppManager.updateApp(app);

        Path testProcessorPath = orgAcmePackagePath.resolve("TestProcessor.java");
        String updatedFileContent = FileUtils.loadFile(testProcessorPath.toFile());
        assertThat(updatedFileContent)
                .contains("io.quarkus.tls.deployment.spi.TlsRegistryBuildItem")
                .doesNotContain("io.quarkus.tls.TlsRegistryBuildItem");
    }

    /**
     * Tests Quarkus CLI update recipes for 3.23.
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.23#schema-management-configuration-properties-gear-white_check_mark">the
     * migration guide note</a>.
     */
    @Test
    void testHibernateSchemaPropertiesUpdated() throws IOException {
        Properties oldProperties = new Properties();
        Properties newProperties = new Properties();

        //== DEFAULT PERSISTENCE UNIT

        oldProperties.put("quarkus.hibernate-orm.database.generation", "drop-and-create");
        newProperties.put("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create");

        oldProperties.put("quarkus.hibernate-orm.database.generation.create-schemas", "true");
        newProperties.put("quarkus.hibernate-orm.schema-management.create-schemas", "true");

        oldProperties.put("quarkus.hibernate-orm.database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.schema-management.halt-on-error", "true");

        oldProperties.put("quarkus.hibernate-orm.database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.schema-management.halt-on-error", "true");

        //== NAMED PERSISTENCE UNITS

        oldProperties.put("quarkus.hibernate-orm.super.database.generation", "drop-and-create");
        newProperties.put("quarkus.hibernate-orm.super.schema-management.strategy", "drop-and-create");

        oldProperties.put("quarkus.hibernate-orm.super.database.generation.create-schemas", "true");
        newProperties.put("quarkus.hibernate-orm.super.schema-management.create-schemas", "true");

        oldProperties.put("quarkus.hibernate-orm.super.database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.super.schema-management.halt-on-error", "true");

        oldProperties.put("quarkus.hibernate-orm.super.database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.super.schema-management.halt-on-error", "true");

        oldProperties.put("quarkus.hibernate-orm.\"fantastic\".database.generation", "drop-and-create");
        newProperties.put("quarkus.hibernate-orm.\"fantastic\".schema-management.strategy", "drop-and-create");

        oldProperties.put("quarkus.hibernate-orm.\"fantastic\".database.generation.create-schemas", "true");
        newProperties.put("quarkus.hibernate-orm.\"fantastic\".schema-management.create-schemas", "true");

        oldProperties.put("quarkus.hibernate-orm.\"fantastic\".database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.\"fantastic\".schema-management.halt-on-error", "true");

        oldProperties.put("quarkus.hibernate-orm.\"fantastic\".database.generation.halt-on-error", "true");
        newProperties.put("quarkus.hibernate-orm.\"fantastic\".schema-management.halt-on-error", "true");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, newProperties);
    }

    /**
     * Tests Quarkus CLI update recipes for 3.26.
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.26#enable--enabled-in-configuration-properties-gear-white_check_mark">the
     * migration guide note</a>.
     */
    @Test
    void testEnableToEnabledPropertiesRenaming() throws IOException {
        Properties oldProperties = new Properties();
        Properties newProperties = new Properties();

        oldProperties.put("quarkus.keycloak.policy-enforcer.enable", "true");
        newProperties.put("quarkus.keycloak.policy-enforcer.enabled", "true");

        oldProperties.put("quarkus.log.console.enable", "true");
        newProperties.put("quarkus.log.console.enabled", "true");

        oldProperties.put("quarkus.log.console.async", "true");
        newProperties.put("quarkus.log.console.async.enabled", "true");

        oldProperties.put("quarkus.log.console.async.enable", "true");
        newProperties.put("quarkus.log.console.async.enabled", "true");

        oldProperties.put("quarkus.log.file.enable", "true");
        newProperties.put("quarkus.log.file.enabled", "true");

        oldProperties.put("quarkus.log.file.async.enable", "true");
        newProperties.put("quarkus.log.file.async.enabled", "true");

        oldProperties.put("quarkus.log.syslog.enable", "true");
        newProperties.put("quarkus.log.syslog.enabled", "true");

        oldProperties.put("quarkus.log.syslog.async.enable", "true");
        newProperties.put("quarkus.log.syslog.async.enabled", "true");

        oldProperties.put("quarkus.log.socket.enable", "true");
        newProperties.put("quarkus.log.socket.enabled", "true");

        oldProperties.put("quarkus.log.socket.async.enable", "true");
        newProperties.put("quarkus.log.socket.async.enabled", "true");

        oldProperties.put("quarkus.snapstart.enable", "true");
        newProperties.put("quarkus.snapstart.enabled", "true");

        oldProperties.put("quarkus.smallrye-health.ui.enable", "true");
        newProperties.put("quarkus.smallrye-health.ui.enabled", "true");

        oldProperties.put("quarkus.smallrye-graphql.ui.enable", "true");
        newProperties.put("quarkus.smallrye-graphql.ui.enabled", "true");

        oldProperties.put("quarkus.smallrye-openapi.enable", "true");
        newProperties.put("quarkus.smallrye-openapi.enabled", "true");

        oldProperties.put("quarkus.swagger-ui.enable", "true");
        newProperties.put("quarkus.swagger-ui.enabled", "true");

        oldProperties.put("quarkus.log.handler.console.whatever.enable", "true");
        newProperties.put("quarkus.log.handler.console.whatever.enabled", "true");

        oldProperties.put("quarkus.log.handler.console.\"whatever\".enable", "true");
        newProperties.put("quarkus.log.handler.console.\"whatever\".enabled", "true");

        oldProperties.put("quarkus.log.handler.file.whatever.enable", "true");
        newProperties.put("quarkus.log.handler.file.whatever.enabled", "true");

        oldProperties.put("quarkus.log.handler.file.\"whatever\".enable", "true");
        newProperties.put("quarkus.log.handler.file.\"whatever\".enabled", "true");

        oldProperties.put("quarkus.log.handler.syslog.whatever.enable", "true");
        newProperties.put("quarkus.log.handler.syslog.whatever.enabled", "true");

        oldProperties.put("quarkus.log.handler.syslog.\"whatever\".enable", "true");
        newProperties.put("quarkus.log.handler.syslog.\"whatever\".enabled", "true");

        oldProperties.put("quarkus.log.handler.socket.whatever.enable", "true");
        newProperties.put("quarkus.log.handler.socket.whatever.enabled", "true");

        oldProperties.put("quarkus.log.handler.socket.\"whatever\".enable", "true");
        newProperties.put("quarkus.log.handler.socket.\"whatever\".enabled", "true");

        oldProperties.put("quarkus.log.handler.console.whatever.async.enable", "true");
        newProperties.put("quarkus.log.handler.console.whatever.async.enabled", "true");

        oldProperties.put("quarkus.log.handler.console.\"whatever\".async.enable", "true");
        newProperties.put("quarkus.log.handler.console.\"whatever\".async.enabled", "true");

        oldProperties.put("quarkus.log.handler.file.whatever.async.enable", "true");
        newProperties.put("quarkus.log.handler.file.whatever.async.enabled", "true");

        oldProperties.put("quarkus.log.handler.file.\"whatever\".async.enable", "true");
        newProperties.put("quarkus.log.handler.file.\"whatever\".async.enabled", "true");

        oldProperties.put("quarkus.log.handler.syslog.whatever.async.enable", "true");
        newProperties.put("quarkus.log.handler.syslog.whatever.async.enabled", "true");

        oldProperties.put("quarkus.log.handler.syslog.\"whatever\".async.enable", "true");
        newProperties.put("quarkus.log.handler.syslog.\"whatever\".async.enabled", "true");

        oldProperties.put("quarkus.log.handler.socket.whatever.async.enable", "true");
        newProperties.put("quarkus.log.handler.socket.whatever.async.enabled", "true");

        oldProperties.put("quarkus.log.handler.socket.\"whatever\".async.enable", "true");
        newProperties.put("quarkus.log.handler.socket.\"whatever\".async.enabled", "true");

        QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, newProperties);
    }

    /**
     * Tests Quarkus CLI update recipes for 3.24 dealing with method and field updates.
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.24#jakarta-persistence-hibernate-orm">the
     * migration guide note</a>.
     */
    @Test
    void testUpdateToHibernateOrm7MethodAndFieldUpdates() {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("hibernate-orm");

        Path orgAcmePackagePath = app.getServiceFolder().resolve("src").resolve("main").resolve("java").resolve("org")
                .resolve("acme");
        File fileWithChangedHibernate = new File(
                "src/test/resources/quarkus327/sources/hibernate/HibernateOrm7SessionChanges.java");
        FileUtils.copyFileTo(fileWithChangedHibernate, orgAcmePackagePath);

        quarkusCLIAppManager.updateApp(app);

        File fileUpdatedByQuarkusCli = orgAcmePackagePath.resolve("HibernateOrm7SessionChanges.java").toFile();
        String updatedFileContent = FileUtils.loadFile(fileUpdatedByQuarkusCli);
        assertThat(updatedFileContent)
                // test CascadeType constant 'REMOVE' was renamed to 'DELETE'
                .contains("var ignored = CascadeType.REMOVE;")
                .doesNotContain("var ignored = CascadeType.DELETE;")
                // test method 'update' is rewritten to 'merge'
                .contains("session.merge(new MyEntity(12L));")
                .doesNotContain("session.update(new MyEntity(12L));")
                // test method 'save' is rewritten to 'persist'
                .contains("session.persist(new MyEntity(10L));")
                .doesNotContain("session.save(new MyEntity(10L));")
                .contains("session.persist(\"MyEntity\", new MyEntity(11L));")
                .doesNotContain("session.save(\"MyEntity\", new MyEntity(11L));")
                // test method 'delete' is rewritten to 'remove'
                .contains("session.remove(9L);")
                .doesNotContain("session.delete(9L);")
                // test method 'get' is rewritten to 'find'
                .contains("session.find(MyEntity.class, 7L);")
                .doesNotContain("session.get(MyEntity.class, 7L);")
                .contains("session.find(\"MyEntity\", 8L);")
                .doesNotContain("session.get(\"MyEntity\", 8L);")
                // test method 'load' is rewritten to 'get'
                .contains("session.get(MyEntity.class, 3L, LockMode.OPTIMISTIC);")
                .doesNotContain("session.load(MyEntity.class, 3L, LockMode.OPTIMISTIC);")
                .contains("session.get(\"MyEntity\", 4L, LockMode.OPTIMISTIC);")
                .doesNotContain("session.load(\"MyEntity\", 4L, LockMode.OPTIMISTIC);")
                .contains("session.get(MyEntity.class, 5L, LockOptions.READ);")
                .doesNotContain("session.load(MyEntity.class, 5L, LockOptions.READ);")
                .contains("session.get(\"MyEntity\", 6L, LockOptions.READ);")
                .doesNotContain("session.load(\"MyEntity\", 6L, LockOptions.READ);")
                // test method 'load' is rewritten to 'getReference'
                .contains("session.getReference(MyEntity.class, 1L);")
                .doesNotContain("session.load(MyEntity.class, 1L);")
                .contains("session.getReference(\"MyEntity\", 2L);")
                .doesNotContain("session.load(\"MyEntity\", 2L);");
    }

    /**
     * Tests Quarkus CLI update recipes for 3.24 migration of the annotation processor
     * paths from 'hibernate-jpamodelgen' to 'hibernate-processor'. See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.24#jakarta-persistence-hibernate-orm">the
     * migration guide note</a>.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "org.hibernate", "org.hibernate.orm"
    })
    void testHibernateJpaModelGenToProcessorUpdate(String originalProcessorGroupId) throws XmlPullParserException, IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        /*
         * Add following configuration to the 'maven-compiler-plugin' plugin:
         *
         * <configuration>
         * <annotationProcessorPaths>
         * <path>
         * <groupId>${originalProcessorGroupId}</groupId>
         * <artifactId>hibernate-jpamodelgen</artifactId>
         * </path>
         * </annotationProcessorPaths>
         * </configuration>
         */
        Xpp3Dom config = new Xpp3Dom("configuration");
        Xpp3Dom annotationProcessorPaths = new Xpp3Dom("annotationProcessorPaths");
        config.addChild(annotationProcessorPaths);
        Xpp3Dom path = new Xpp3Dom("path");
        annotationProcessorPaths.addChild(path);
        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue(originalProcessorGroupId);
        path.addChild(groupId);
        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue("hibernate-jpamodelgen");
        path.addChild(artifactId);
        addConfigurationToBuildPlugin(app, "maven-compiler-plugin", config);

        File pomFile = app.getFileFromApplication("pom.xml");
        String originalPomFileContent = FileUtils.loadFile(pomFile);
        assertThat(originalPomFileContent)
                .doesNotContain("<artifactId>hibernate-processor</artifactId>")
                .contains("<artifactId>hibernate-jpamodelgen</artifactId>");

        quarkusCLIAppManager.updateApp(app);

        String pomFileAfterAppUpdate = FileUtils.loadFile(pomFile);
        assertThat(pomFileAfterAppUpdate)
                .contains("<artifactId>hibernate-processor</artifactId>")
                .doesNotContain("<artifactId>hibernate-jpamodelgen</artifactId>")
                .contains("<groupId>org.hibernate.orm</groupId>");
    }

    /**
     * Tests Quarkus CLI update recipes for 3.24 migration of the 'hibernate-jpamodelgen' dependency
     * to the 'hibernate-processor' annotation processor path. See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.24#jakarta-persistence-hibernate-orm">the
     * migration guide note</a>.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "org.hibernate", "org.hibernate.orm"
    })
    void testHibernateJpaModelGenDependencyToProcessorUpdate(String jpaModelGenGroupId)
            throws XmlPullParserException, IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        var hibernateJpaModelGen = new Dependency();
        hibernateJpaModelGen.setGroupId(jpaModelGenGroupId);
        hibernateJpaModelGen.setArtifactId("hibernate-jpamodelgen");
        QuarkusCLIUtils.addDependenciesToPom(app, List.of(hibernateJpaModelGen));

        File pomFile = app.getFileFromApplication("pom.xml");
        String originalPomFileContent = FileUtils.loadFile(pomFile);
        assertThat(originalPomFileContent)
                .contains("<artifactId>hibernate-jpamodelgen</artifactId>")
                .doesNotContain("<artifactId>hibernate-processor</artifactId>")
                .doesNotContain("<annotationProcessorPaths>");

        quarkusCLIAppManager.updateApp(app);

        // check that the 'hibernate-jpamodelgen' dependency is removed
        // and there is a newly added annotation processor path for the 'hibernate-processor'
        String pomFileAfterAppUpdate = FileUtils.loadFile(pomFile);
        assertThat(pomFileAfterAppUpdate)
                .contains("<annotationProcessorPaths>")
                .contains("<artifactId>hibernate-processor</artifactId>")
                .doesNotContain("<artifactId>hibernate-jpamodelgen</artifactId>")
                .doesNotContain("<groupId>org.hibernate</groupId>");
    }

    private static void addConfigurationToBuildPlugin(QuarkusCliRestService app, String pluginArtifactId, Xpp3Dom config)
            throws XmlPullParserException, IOException {
        Model pom = QuarkusCLIUtils.getPom(app);

        Plugin plugin = pom.getBuild().getPlugins()
                .stream()
                .filter(p -> pluginArtifactId.equals(p.getArtifactId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Plugin artifact not found: " + pluginArtifactId));

        plugin.setConfiguration(config);

        QuarkusCLIUtils.savePom(app, pom);
    }
}
