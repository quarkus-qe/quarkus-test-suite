package io.quarkus.ts.cyclonedx;

import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertEmbeddedSbomFramework;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertFrameworkBomProvides;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomFromNativeImage;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomRootNodeFromNativeImage;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@EnabledOnNative
@EnabledOnQuarkusVersion(version = ".*redhat.*", reason = "Needs product platform")
public class CycloneDxNativeProductCpeIT {

    @QuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    @Test
    public void embededSBOM() throws Exception {
        Bom sbom = getSbomFromNativeImage();
        String frameworkBomRef = assertEmbeddedSbomFramework(sbom);

        JsonNode root = getSbomRootNodeFromNativeImage();
        assertFrameworkBomProvides(root, frameworkBomRef);
    }
}
