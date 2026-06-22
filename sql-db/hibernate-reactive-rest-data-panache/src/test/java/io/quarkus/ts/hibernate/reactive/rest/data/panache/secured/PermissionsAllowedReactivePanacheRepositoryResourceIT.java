package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-7179")
@QuarkusScenario
public class PermissionsAllowedReactivePanacheRepositoryResourceIT
        extends AbstractPermissionsAllowedReactivePanacheResourceIT {
    private static final String BASE_URL = "/secured/repository";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
}
