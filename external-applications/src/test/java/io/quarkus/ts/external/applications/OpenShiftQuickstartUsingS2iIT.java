package io.quarkus.ts.external.applications;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@DisabledOnNative(reason = "S2i is not supported on Native yet: https://github.com/quarkus-qe/quarkus-test-framework/issues/206")
@DisabledOnQuarkusSnapshot(reason = "999-SNAPSHOT is not available in the Maven repositories in OpenShift")
@OpenShiftScenario
public class OpenShiftQuickstartUsingS2iIT {
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-quickstarts.git", contextDir = "getting-started")
    static final RestService app = new RestService();

    @Test
    public void verify() {
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
