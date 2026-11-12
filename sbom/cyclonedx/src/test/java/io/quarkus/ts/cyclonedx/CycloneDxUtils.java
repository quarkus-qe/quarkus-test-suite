package io.quarkus.ts.cyclonedx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.parsers.JsonParser;
import org.junit.jupiter.api.Assumptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.deployment.util.ContainerRuntimeUtil;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class CycloneDxUtils {

    static void assertEmbeddedSbomComponents(Bom bom) {
        assertThat(bom).isNotNull();
        assertThat(bom.getMetadata()).isNotNull();
        assertThat(bom.getMetadata().getComponent()).isNotNull();

        final List<Component> components = bom.getComponents();
        assertThat(components).isNotEmpty();

        assertComponent(components, "io.quarkus", "quarkus-rest");
        assertComponent(components, "io.quarkus", "quarkus-rest-deployment");
        assertComponent(components, "io.quarkus", "quarkus-rest-jackson");
        assertComponent(components, "io.quarkus", "quarkus-rest-jackson-deployment");
        assertComponent(components, "io.quarkus", "quarkus-cyclonedx");
        assertComponent(components, "io.quarkus", "quarkus-cyclonedx-deployment");
    }

    static void assertComponent(List<Component> components, String group, String name) {
        final Component component = components.stream()
                .filter(c -> group != null ? group.equals(c.getGroup()) && name.equals(c.getName()) : name.equals(c.getName()))
                .findFirst()
                .orElse(null);
        assertThat(component)
                .as("Expected component %s:%s in SBOM", group, name)
                .isNotNull();
    }

    static String assertEmbeddedSbomFramework(Bom bom) {
        assertThat(bom).isNotNull();
        assertThat(bom.getMetadata()).isNotNull();
        assertThat(bom.getMetadata().getComponent()).isNotNull();

        final List<Component> components = bom.getComponents();
        assertThat(components).isNotEmpty();

        List<Component> frameworkComponents = components.stream()
                .filter(c -> c.getType().equals(Component.Type.FRAMEWORK))
                .toList();

        /*
         * "type" : "framework",
         * "bom-ref" : "pkg:maven/com.redhat.quarkus.platform/quarkus-bom@3.39.3.temporary-redhat-00001?type=pom",
         * "group" : "com.redhat.quarkus.platform",
         * "name" : "quarkus-bom",
         * "version" : "3.39.3.temporary-redhat-00001",
         * "description" : "Red Hat Build of Quarkus - Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM",
         * "scope" : "excluded",
         * "cpe" : "cpe:/a:redhat:quarkus:3.40",
         * "purl" : "pkg:maven/com.redhat.quarkus.platform/quarkus-bom@3.39.3.temporary-redhat-00001?type=pom",
         */
        assertThat(frameworkComponents).size().as("Exactly one framework component is expected").isEqualTo(1);
        Component quarkusBomComponent = frameworkComponents.get(0);

        String frameworkBomRef = quarkusBomComponent.getBomRef();
        assertThat(frameworkBomRef).contains("/quarkus-bom@");
        assertThat(quarkusBomComponent.getGroup()).contains("quarkus.platform");
        assertThat(quarkusBomComponent.getName()).isEqualTo("quarkus-bom");
        assertThat(quarkusBomComponent.getVersion()).isEqualTo(QuarkusProperties.getVersion());
        assertThat(quarkusBomComponent.getDescription()).contains("Build of Quarkus");
        assertThat(quarkusBomComponent.getCpe()).startsWith("cpe:/a:redhat:quarkus:");
        assertThat(quarkusBomComponent.getPurl()).startsWith("pkg:maven/com.redhat.quarkus.platform/quarkus-bom@");

        return frameworkBomRef;

    }

    static void assertFrameworkBomProvides(JsonNode root, String frameworkBomRef) throws IOException {
        JsonNode dependencies = root.get("dependencies");

        List<String> sbomDefinedDependenciesRefs = new ArrayList<>();
        List<String> frameworkBomProvidesRefs = new ArrayList<>();
        for (JsonNode item : dependencies) {
            if (item.get("ref").asText().equals(frameworkBomRef)) {
                assertThat(item.has("provides")).isTrue();

                JsonNode provides = item.get("provides");
                assertThat(provides).hasSizeGreaterThan(0);
                for (JsonNode enrty : provides) {
                    frameworkBomProvidesRefs.add(enrty.asText());
                }
            } else {
                sbomDefinedDependenciesRefs.add(item.get("ref").asText());
            }
        }

        List<String> dependenciesWithoutEntryInProvides = sbomDefinedDependenciesRefs.stream()
                .filter(dep -> !dep.equals("pkg:maven/io.quarkus.ts.qe/sbom-cyclonedx@1.0.0-SNAPSHOT?type=jar"))
                .filter(dep -> !frameworkBomProvidesRefs.contains(dep))
                // TODO remove once https://redhat.atlassian.net/browse/QUARKUS-9312 is fixed
                .filter(dep -> !dep.startsWith("pkg:maven/com.aayushatharva.brotli4j/native-"))
                .filter(dep -> !dep.startsWith("pkg:maven/io.quarkus/quarkus-cyclonedx-endpoint"))
                .toList();

        List<String> providesEntriesWithoutEntryInDependencies = frameworkBomProvidesRefs.stream()
                .filter(dep -> !sbomDefinedDependenciesRefs.contains(dep)).toList();

        if (dependenciesWithoutEntryInProvides.size() > 0 && providesEntriesWithoutEntryInDependencies.size() > 0) {
            fail("There are dependencies not covered in 'provides' entry of quarkus-bom dependency\n" +
                    failureMessageFor(dependenciesWithoutEntryInProvides) + "\n" +
                    "There are entries defined in 'provides' of quarkus-bom but not covered in dependencies section\n" +
                    failureMessageFor(providesEntriesWithoutEntryInDependencies));
        } else if (dependenciesWithoutEntryInProvides.size() > 0) {
            fail("There are dependencies not covered in 'provides' entry of quarkus-bom dependency\n" +
                    failureMessageFor(dependenciesWithoutEntryInProvides));
        } else if (providesEntriesWithoutEntryInDependencies.size() > 0) {
            fail("There are entries defined in 'provides' of quarkus-bom but not covered in dependencies section\n" +
                    failureMessageFor(providesEntriesWithoutEntryInDependencies));
        }
    }

    private static String failureMessageFor(List<String> list) {
        return "This is the list:\n" + list.stream()
                .map(s -> " - " + s)
                .collect(Collectors.joining("\n"));
    }

    static Bom getSbomFromJar() throws IOException, ParseException {
        try (InputStream is = getSbomInputStream()) {
            return new JsonParser().parse(is);
        }
    }

    static JsonNode getSbomRootNodeFromJar() throws IOException, ParseException {
        try (InputStream is = getSbomInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(is);
        }
    }

    private static InputStream getSbomInputStream() throws IOException, ParseException {
        // If (ever) multiple applications get tested in this module, the path to the jar would be
        // app.getServiceFolder().toAbsolutePath().resolve("mvn-build/target/quarkus-app/quarkus/generated-bytecode.jar");
        final Path generatedJar = Path.of("target/quarkus-app/quarkus/generated-bytecode.jar");
        assertThat(generatedJar.toFile()).exists();

        String resourceName = "META-INF/sbom/dependency.cdx.json";
        JarFile jar = new JarFile(generatedJar.toFile());
        JarEntry entry = jar.getJarEntry(resourceName);
        assertThat(entry)
                .as("Expected resource %s in %s", resourceName, generatedJar.getFileName())
                .isNotNull();
        return jar.getInputStream(entry);
    }

    static Bom getSbomFromNativeImage() throws Exception {
        Bom syftBom = new JsonParser().parse(getSbomStringFromNativeImage().getBytes(StandardCharsets.UTF_8));
        assertThat(syftBom).isNotNull();
        return syftBom;
    }

    static JsonNode getSbomRootNodeFromNativeImage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String sbomContent = getSbomStringFromNativeImage();
        Files.writeString(Path.of("target/embedded-sbom-native.json"), sbomContent);
        return mapper.readTree(sbomContent);
    }

    private static String getSbomStringFromNativeImage() throws Exception {
        // If (ever) multiple applications get tested in this module, the path to the jar would be
        // app.getServiceFolder().toAbsolutePath().resolve("mvn-build/target/sbom-cyclonedx-1.0.0-SNAPSHOT-runner");
        final Path nativeImage = Path.of("target/sbom-cyclonedx-1.0.0-SNAPSHOT-runner");
        assertThat(nativeImage.toFile()).exists();

        final ContainerRuntimeUtil.ContainerRuntime containerRuntime = ContainerRuntimeUtil.detectContainerRuntime(false);
        Assumptions.assumeTrue(containerRuntime != ContainerRuntimeUtil.ContainerRuntime.UNAVAILABLE,
                "Skipping syft verification since no container runtime is available");

        final String runtime = containerRuntime.getExecutableName();
        // podman run --rm -ti --entrypoint /usr/bin/native-image-utils registry.access.redhat.com/quarkus/mandrel-25-rhel9:25.0 help
        final ProcessBuilder pb = new ProcessBuilder(
                runtime, "run", "--rm", "--entrypoint", "/usr/bin/native-image-utils",
                "-v", nativeImage.toAbsolutePath() + ":/binary:ro,z",
                "registry.access.redhat.com/quarkus/mandrel-25-rhel9:25.0",
                "extract-sbom", "--image-path=/binary");
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

        final Process process = pb.start();
        final String output;
        try (InputStream is = process.getInputStream()) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            is.transferTo(baos);
            output = baos.toString(StandardCharsets.UTF_8);
        }
        final int exitCode = process.waitFor();
        assertThat(exitCode)
                .as("syft exited with code %d", exitCode)
                .isZero();

        return output;
    }

}
