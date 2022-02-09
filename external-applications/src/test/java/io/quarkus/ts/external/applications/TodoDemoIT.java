package io.quarkus.ts.external.applications;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@DisabledOnNative(reason = "This scenario is using uber-jar, so it's incompatible with Native")
@QuarkusScenario
public class TodoDemoIT {
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/todo-demo-app.git", mavenArgs = "-Dquarkus.package.type=uber-jar -DskipTests=true -Dquarkus.platform.artifact-id=quarkus-bom -Dquarkus.platform.version=${QUARKUS_VERSION} -Dquarkus-plugin.version=${QUARKUS_PLUGIN_VERSION}")
    static final RestService app = new RestService();

    @Test
    public void verify() {
        app.given()
                .get()
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
