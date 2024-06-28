package io.quarkus.ts.external.applications;

import static io.quarkus.ts.external.applications.AbstractTodoDemoIT.DEFAULT_OPTIONS;
import static io.quarkus.ts.external.applications.AbstractTodoDemoIT.TODO_REPO;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@QuarkusScenario
public class TodoDemoIT extends AbstractTodoDemoIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            // store data in /tmp/psql as in OpenShift we don't have permissions to /var/lib/postgresql/data
            .withProperty("PGDATA", "/tmp/psql");

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, branch = "quarkus3", mavenArgs = "-Dquarkus.package.type=uber-jar"
            + DEFAULT_OPTIONS)
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @GitRepositoryQuarkusApplication(repo = TODO_REPO, branch = "quarkus3", artifact = "todo-backend-1.0-SNAPSHOT.jar", mavenArgs = "-Dquarkus.package.type=uber-jar -Dquarkus.package.add-runner-suffix=false"
            + DEFAULT_OPTIONS)
    static final RestService replaced = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected RestService getReplaced() {
        return replaced;
    }
}
