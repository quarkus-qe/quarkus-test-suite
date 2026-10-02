package io.quarkus.ts.cyclonedx;

import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertEmbeddedSbomComponents;
import static io.quarkus.ts.cyclonedx.CycloneDxUtils.getSbomFromJar;

import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative
public class CycloneDxJvmIT {

    @QuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    @Test
    public void embededSBOM() throws Exception {
        Bom sbom = getSbomFromJar();
        assertEmbeddedSbomComponents(sbom);
    }
}
