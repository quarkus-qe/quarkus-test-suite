package io.quarkus.ts.quarkus.cli.offering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class QuarkusCliOfferingUtils {

    private static final Logger log = Logger.getLogger(QuarkusCliOfferingUtils.class);

    public static final File QUARKUS_CONFIG = Paths.get(System.getProperty("user.home"), ".quarkus", "config.yaml").toFile();
    public static final File QUARKUS_TEST_CONFIG = Paths.get("target", ".quarkus",
            "config.yaml").toFile();

    private static final String QUARKUS_REGISTRY_ID = "testingregistry";
    private static final String LANGCHAIN4J_ARTIFACT_ID_NAME = "quarkus-langchain4j-bom";

    public static String getExtensionLineFromListOutput(QuarkusCliClient.Result result, String extensionArtifactName) {
        return result.getOutput()
                .lines()
                .filter(line -> line.contains(extensionArtifactName))
                .findFirst()
                .orElse(null);
    }

    public static void assertCorrectPlatformBom(File pomFile, String quarkusPlatformGroupId) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomFile)) {
            Model model = reader.read(fileReader);
            Assertions.assertEquals(model.getProperties().get("quarkus.platform.group-id"), quarkusPlatformGroupId,
                    "Unexpected Quarkus platform bom (`quarkus.platform.group-id`) defined in pom.xml of created app.");
        } catch (IOException | XmlPullParserException e) {
            fail(e.getMessage());
        }
    }

    public static void assertCorrectLangChain4jBom(File pomFile, String expectedLangchain4jVersion) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomFile)) {
            Model model = reader.read(fileReader);
            List<Dependency> dependencies = model.getDependencyManagement()
                    .getDependencies()
                    .stream()
                    .filter(dependency -> dependency.getArtifactId().equals(LANGCHAIN4J_ARTIFACT_ID_NAME))
                    .toList();
            assertEquals(1, dependencies.size(), "Langchain4j bom should be present only once");
            assertEquals(expectedLangchain4jVersion, dependencies.get(0).getVersion(),
                    "Langchain4j bom should have " + expectedLangchain4jVersion
                            + " set instead of " + dependencies.get(0).getVersion());
        } catch (IOException | XmlPullParserException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Load data from ~/.quarkus/config.yaml and update them
     *
     * @param offering offering value e.g. ibm, redhat
     * @throws IOException
     */
    public static void updateRegistryConfigFileWithOffering(String offering) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        Map<String, Object> data;
        try (InputStream inputStream = new FileInputStream(QUARKUS_TEST_CONFIG)) {
            data = yaml.load(inputStream);
        }

        updateRegistryConfig((List<Object>) data.get("registries"), offering);

        try (Writer writer = new FileWriter(QUARKUS_TEST_CONFIG)) {
            log.info("Quarkus config in use is: located at " + QUARKUS_TEST_CONFIG.getAbsolutePath()
                    + " and content of config is:\n" + data);
            yaml.dump(data, writer);
        }
    }

    /**
     * Iterate over registries and add offering value to registry with name of {@link #QUARKUS_REGISTRY_ID}
     *
     * @param registries list of all set registries
     * @param offering offering value e.g. ibm, redhat
     */
    private static void updateRegistryConfig(List<Object> registries, String offering) {
        for (Object item : registries) {
            if (item instanceof Map) {
                Map<String, Object> registryMap = (Map<String, Object>) item;
                if (registryMap.containsKey(QUARKUS_REGISTRY_ID)) {
                    // Get the testing registry and set the offering
                    Map<String, Object> details = (Map<String, Object>) registryMap.get(QUARKUS_REGISTRY_ID);
                    details.put("offering", offering);
                    return;
                }
            }
        }
        Assertions.fail(QUARKUS_REGISTRY_ID + " registry is not present in quarkus config");
    }

    public static String getQuarkusVersionWithoutNumberSuffix() {
        return QuarkusProperties.getVersion().replaceAll("-\\d{5}$", "");
    }
}
