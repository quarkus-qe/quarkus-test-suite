package io.quarkus.ts.external.applications;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.restassured.http.ContentType;

@DisabledOnNative(reason = "This scenario is using uber-jar, so it's incompatible with Native")
@QuarkusScenario
public class TodoDemoIT {
    private static final String TODO_REPO = "https://github.com/quarkusio/todo-demo-app.git";
    private static final String VERSIONS = "-Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION} ";
    private static final String DEFAULT_OPTIONS = " -DskipTests=true " + VERSIONS;

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            // store data in /tmp/psql as in OpenShift we don't have permissions to /var/lib/postgresql/data
            .withProperty("PGDATA", "/tmp/psql");

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, mavenArgs = "-Dquarkus.package.type=uber-jar" + DEFAULT_OPTIONS)
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, artifact = "todo-backend-1.0-SNAPSHOT.jar", mavenArgs = "-Dquarkus.package.type=uber-jar -Dquarkus.package.add-runner-suffix=false"
            + DEFAULT_OPTIONS)
    static final RestService replaced = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void startsSuccessfully() {
        app.given()
                .contentType(ContentType.JSON)
                .body("{\"title\": \"Use Quarkus\", \"order\": 1, \"url\": \"https://quarkus.io\"}")
                .post("/api")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void replacedStartsSuccessfully() {
        replaced.given()
                .accept(ContentType.JSON)
                .get("/api")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", is(1))
                .body("title[0]", is("Use Quarkus"));
    }
}
