package io.quarkus.ts.cyclonedx;

import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertEmbeddedSbomComponents;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomFromNativeImage;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@EnabledOnNative
public class CycloneDxNativeIT {

    @QuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    @Test
    public void embededSBOM() throws Exception {
        Bom sbom = getSbomFromNativeImage();
        assertEmbeddedSbomComponents(sbom);
    }
}
