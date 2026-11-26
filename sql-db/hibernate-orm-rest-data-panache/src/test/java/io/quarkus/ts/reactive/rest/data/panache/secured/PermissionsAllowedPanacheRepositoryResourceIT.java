package io.quarkus.ts.reactive.rest.data.panache.secured;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;

@Tag("QUARKUS-7179")
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class PermissionsAllowedPanacheRepositoryResourceIT extends AbstractPermissionsAllowedPanacheResourceIT {
    private static final String BASE_URL = "/secured/repository";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
}
