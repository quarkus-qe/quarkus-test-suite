package io.quarkus.ts.quarkus.cli.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.util.QuarkusCLIUtils;
import io.quarkus.test.util.QuarkusCLIUtils.QuarkusDependency;
import io.quarkus.test.utils.FileUtils;

/**
 * Tests Quarkus CLI update command recipes for changes between 3.27 and 3.33.
 */
@Tag("quarkus-cli")
public class QuarkusCli340UpdatesIT extends AbstractQuarkusCliUpdateIT {

    public QuarkusCli340UpdatesIT() {
        super(new DefaultArtifactVersion("3.33"), new DefaultArtifactVersion("3.40"));
    }

    /**
     * Tests Quarkus CLI update recipe for 3.37 rename of panache next to hibernate data
     * See <a href=
     * https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.37#panache-next--quarkus-data-gear-white_check_mark>
     * the migration guide note</a>.
     */
    @Test
    void testUpdatePanacheNextToHibernateData() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("quarkus-hibernate-panache-next");

        {
            Model pom = QuarkusCLIUtils.getPom(app);
            Dependency testcontainers = new QuarkusDependency("io.quarkus:quarkus-hibernate-panache-next-deployment");
            pom.addDependency(testcontainers);
            QuarkusCLIUtils.savePom(app, pom);
        }

        File pomFile = app.getFileFromApplication("pom.xml");

        String pomFileBeforeUpdate = FileUtils.loadFile(pomFile);
        assertThat(pomFileBeforeUpdate)
                .contains("quarkus-hibernate-panache-next")
                .contains("quarkus-hibernate-panache-next-deployment");

        quarkusCLIAppManager.updateApp(app);

        String pomFileAfterAppUpdate = FileUtils.loadFile(pomFile);
        assertThat(pomFileAfterAppUpdate)
                .contains("quarkus-data-hibernate")
                .doesNotContain("quarkus-hibernate-panache-next")
                .contains("quarkus-data-hibernate-deployment")
                .doesNotContain("quarkus-hibernate-panache-next-deployment");
    }

    /**
     * Tests Quarkus CLI update recipe for 3.38, elasticsearch-rest-client to elasticsearch-rest5-client
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.38#elasticsearch-rest-client">the
     * migration guide note</a>.
     */
    @Test
    void testUpdateToElasticsearchRest5() {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("elasticsearch-rest-client");

        Path orgAcmePackagePath = app.getServiceFolder().resolve("src").resolve("main").resolve("java").resolve("org")
                .resolve("acme");

        File fileWithChangedHibernate = new File(
                "src/test/resources/quarkus340/sources/elasticsearch/ElasticSearchUpdate.java");
        FileUtils.copyFileTo(fileWithChangedHibernate, orgAcmePackagePath);

        quarkusCLIAppManager.updateApp(app);

        File fileUpdatedByQuarkusCli = orgAcmePackagePath.resolve("ElasticSearchUpdate.java").toFile();
        String updatedFileContent = FileUtils.loadFile(fileUpdatedByQuarkusCli);
        assertThat(updatedFileContent)
                // RestClient -> Rest5Client
                .contains("import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;")
                .doesNotContain("import org.elasticsearch.client.RestClient;")
                .contains("private Rest5Client client")
                .doesNotContain("private RestClient client")
                .contains("setClient(Rest5Client client)")
                .doesNotContain("setClient(RestClient client)")
                // RestClientBuilder -> Rest5ClientBuilder
                .contains("import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;")
                .doesNotContain("import org.elasticsearch.client.RestClientBuilder;")
                .contains("private Rest5ClientBuilder builder")
                .doesNotContain("private RestClientBuilder builder")
                // Request and Response
                .contains("import co.elastic.clients.transport.rest5_client.low_level.Request;")
                .contains("import co.elastic.clients.transport.rest5_client.low_level.Response;")
                .doesNotContain("import org.elasticsearch.client.Request;")
                .doesNotContain("import org.elasticsearch.client.Response;")
                // RestClientBuilder.HttpClientConfigCallback -> ElasticsearchClientConfigConfigurer
                .contains("import io.quarkus.elasticsearch.restclient.lowlevel.ElasticsearchClientConfigConfigurer;")
                .contains("MyConfigurator implements ElasticsearchClientConfigConfigurer")
                .doesNotContain("import org.elasticsearch.client.RestClientBuilder;")
                .doesNotContain("MyConfigurator implements RestClientBuilder.HttpClientConfigCallback");
    }

    /**
     * Tests Quarkus CLI update recipe for 3.39, panache update to data
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.39#packages-and-types-gear-white_check_mark">the
     * migration guide note</a>.
     */
    @Test
    void testUpdateToHibernateDataTypesAndPackage() {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("quarkus-hibernate-panache-next",
                "quarkus-hibernate-orm-panache", "quarkus-hibernate-reactive");

        Path orgAcmePackagePath = app.getServiceFolder().resolve("src").resolve("main").resolve("java").resolve("org")
                .resolve("acme");
        String filesBasePath = "src/test/resources/quarkus340/sources/hibernate/";

        List<String> fileNames = Arrays.asList("MyPanacheEntity.java", "MyPanacheEntityManaged.java",
                "MyPanacheEntityReactive.java", "MyPanacheEntityStateless.java", "MyPanacheEntityReactiveStateless.java");

        copyFiles(fileNames, filesBasePath, orgAcmePackagePath, app);

        quarkusCLIAppManager.updateApp(app);

        File fileUpdatedByQuarkusCli = orgAcmePackagePath.resolve("MyPanacheEntity.java").toFile();
        String updatedFileContent = FileUtils.loadFile(fileUpdatedByQuarkusCli);
        assertThat(updatedFileContent)
                // 1. Check imports and package rename
                .contains("import io.quarkus.data.hibernate.")
                .doesNotContain("import io.quarkus.hibernate.panache.")
                // PanacheEntity -> ManagedEntity
                .contains("extends ManagedEntity")
                .contains("import io.quarkus.data.hibernate.ManagedEntity;")
                .doesNotContain("extends PanacheEntity")
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheEntity;")
                // PanacheRepository -> ManagedRepository
                .contains("extends ManagedRepository<Object>")
                .contains("import io.quarkus.data.hibernate.ManagedRepository;")
                .doesNotContain("extends PanacheRepository<Object>")
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheRepository;")
                // PanacheQuery -> DataQuery
                .contains("private DataQuery<Object, Object, Object, Object> query")
                .contains("import io.quarkus.data.hibernate.DataQuery;")
                .doesNotContain("private PanacheQuery<Object, Object, Object, Object> query")
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheQuery;")
                // PanacheEntityMarker -> EntitySwitcher
                .contains("private EntitySwitcher marker")
                .contains("import io.quarkus.data.hibernate.EntitySwitcher;")
                .doesNotContain("private PanacheEntityMarker marker")
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheEntityMarker;")
                // Panache -> QuarkusData
                .contains("MyPanache extends QuarkusData")
                .contains("import io.quarkus.data.hibernate.QuarkusData;")
                .doesNotContain("MyPanache extends Panache")
                .doesNotContain("import io.quarkus.hibernate.panache.Panache;")
                // Check PanacheBlockingQuery import is only changing
                .contains("import io.quarkus.data.hibernate.blocking.PanacheBlockingQuery;")
                .doesNotContain("import io.quarkus.hibernate.panache.blocking.PanacheBlockingQuery;")
                .contains("private PanacheBlockingQuery<Object> blockingQuery");

        checkEntityUpdatedFile(orgAcmePackagePath, "MyPanacheEntityReactive.java", "Reactive",
                "ManagedEntity.Reactive", "ManagedRepository.Reactive");
        checkEntityUpdatedFile(orgAcmePackagePath, "MyPanacheEntityManaged.java", "Managed", "ManagedEntity",
                "ManagedRepository");
        checkEntityUpdatedFile(orgAcmePackagePath, "MyPanacheEntityStateless.java", "Stateless", "RecordEntity",
                "RecordRepository");
        checkEntityUpdatedFile(orgAcmePackagePath, "MyPanacheEntityReactiveStateless.java", "Reactive.Stateless",
                "RecordEntity.Reactive", "RecordRepository.Reactive");
    }

    /**
     * Tests Quarkus CLI update recipe for 3.39, panache update annotationProcessorPaths
     * See <a href=
     * "https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.39#packages-and-types-gear-white_check_mark">the
     * migration guide note</a>.
     */
    @Test
    void testHibernateJpaModelGenToProcessorUpdate() throws XmlPullParserException, IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        Xpp3Dom config = new Xpp3Dom("configuration");
        Xpp3Dom annotationProcessorPaths = new Xpp3Dom("annotationProcessorPaths");
        config.addChild(annotationProcessorPaths);
        Xpp3Dom path = new Xpp3Dom("path");
        annotationProcessorPaths.addChild(path);
        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue("org.hibernate");
        path.addChild(groupId);
        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue("hibernate-jpamodelgen");
        path.addChild(artifactId);
        addConfigurationToBuildPlugin(app, "maven-compiler-plugin", config);

        quarkusCLIAppManager.updateApp(app);

        File pomFile = app.getFileFromApplication("pom.xml");
        String pomFileAfterAppUpdate = FileUtils.loadFile(pomFile);
        assertThat(pomFileAfterAppUpdate)
                .contains("<artifactId>quarkus-data-processor</artifactId>")
                .contains("<groupId>io.quarkus</groupId>")
                .doesNotContain("<artifactId>hibernate-jpamodelgen</artifactId>")
                .doesNotContain("<groupId>org.hibernate</groupId>");
    }

    private void checkEntityUpdatedFile(Path packagePath, String fileName, String originalType, String expectedEntityType,
            String expectedRepositoryType) {
        // To check import we need to remove `Reactive` part
        String importExpectedEntityType = expectedEntityType.replace(".Reactive", "");
        String importExpectedRepositoryType = expectedRepositoryType.replace(".Reactive", "");
        File fileUpdatedByQuarkusCli = packagePath.resolve(fileName).toFile();
        String updatedFileContent = FileUtils.loadFile(fileUpdatedByQuarkusCli);
        assertThat(updatedFileContent)
                .contains("import io.quarkus.data.hibernate.")
                .doesNotContain("import io.quarkus.hibernate.panache.")
                // PanacheEntity.*
                .contains("implements " + expectedEntityType + ".CustomId")
                .contains("import io.quarkus.data.hibernate." + importExpectedEntityType)
                .doesNotContain("implements PanacheEntity." + originalType)
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheEntity;")
                // PanacheRepository.*
                .contains("extends " + expectedRepositoryType + ".CustomId<Object, Long>")
                .contains("import io.quarkus.data.hibernate." + importExpectedRepositoryType)
                .doesNotContain("extends PanacheRepository." + originalType + "<Object, Long>")
                .doesNotContain("import io.quarkus.hibernate.panache.PanacheRepository;");
    }

    private void copyFiles(List<String> files, String basePath, Path packagePath, QuarkusCliRestService app) {
        for (String file : files) {
            File fileWithChangedHibernate = new File(basePath + file);
            FileUtils.copyFileTo(fileWithChangedHibernate, packagePath);
        }
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
