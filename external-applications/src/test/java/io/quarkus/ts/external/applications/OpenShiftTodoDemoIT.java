package io.quarkus.ts.external.applications;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@DisabledOnNative
@DisabledOnQuarkusSnapshot(reason = "999-SNAPSHOT is not available in the Maven repositories in OpenShift")
@OpenShiftScenario
public class OpenShiftTodoDemoIT {
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/todo-demo-app.git", mavenArgs = "-Dquarkus.package.type=uber-jar -DskipTests=true")
    static final RestService app = new RestService();

    @Test
    public void verify() {
        app.given()
                .get()
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
