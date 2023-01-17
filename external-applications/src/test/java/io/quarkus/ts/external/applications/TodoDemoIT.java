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
    private static final String TODO_REPO = "https://github.com/quarkusio/todo-demo-app.git";
    private static final String VERSIONS = "-Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_VERSION} ";
    private static final String DEFAULT_OPTIONS = "-DskipTests=true " + VERSIONS;

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, mavenArgs = "-Dquarkus.package.type=uber-jar " + DEFAULT_OPTIONS)
    static final RestService app = new RestService();

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, artifact = "todo-backend-1.0-SNAPSHOT.jar", mavenArgs = "-Dquarkus.package.type=uber-jar -Dquarkus.package.add-runner-suffix=false"
            + DEFAULT_OPTIONS)
    static final RestService replaced = new RestService();

    @Test
    public void startsSuccessfully() {
        app.given()
                .get()
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void replacedStartsSuccessfully() {
        replaced.given()
                .get()
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
