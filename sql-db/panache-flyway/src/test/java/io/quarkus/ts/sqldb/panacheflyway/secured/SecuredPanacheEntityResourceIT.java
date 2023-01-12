package io.quarkus.ts.sqldb.panacheflyway.secured;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-2788")
@QuarkusScenario
public class SecuredPanacheEntityResourceIT extends AbstractSecuredPanacheResourceIT {
    private static final String BASE_URL = "/secured/entity";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
}
