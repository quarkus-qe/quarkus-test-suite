package io.quarkus.ts.reactive.rest.data.panache.secured;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;

@Tag("QUARKUS-2788")
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class SecuredPanacheEntityResourceIT extends AbstractSecuredPanacheResourceIT {
    private static final String BASE_URL = "/secured/entity";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
}
