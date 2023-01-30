package io.quarkus.ts.sqldb.panacheflyway.secured;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-2788")
@QuarkusScenario
public class SecuredPanacheRepositoryResourceIT extends AbstractSecuredPanacheResourceIT {
    private static final String BASE_URL = "/secured/repository";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
}
