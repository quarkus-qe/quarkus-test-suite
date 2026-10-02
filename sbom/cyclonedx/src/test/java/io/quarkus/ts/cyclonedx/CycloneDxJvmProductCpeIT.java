package io.quarkus.ts.cyclonedx;

import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertEmbeddedSbomFramework;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertFrameworkBomProvides;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomFromJar;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomRootNodeFromJar;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative
@EnabledOnQuarkusVersion(version = ".*redhat.*", reason = "Needs product platform")
public class CycloneDxJvmProductCpeIT {

    @QuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    @Test
    public void embededSBOM() throws Exception {
        Bom sbom = getSbomFromJar();
        String frameworkBomRef = assertEmbeddedSbomFramework(sbom);

        JsonNode root = getSbomRootNodeFromJar();
        assertFrameworkBomProvides(root, frameworkBomRef);
    }
}
