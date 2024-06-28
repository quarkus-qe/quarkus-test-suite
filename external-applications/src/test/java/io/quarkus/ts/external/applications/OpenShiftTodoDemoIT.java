package io.quarkus.ts.external.applications;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
public class OpenShiftTodoDemoIT extends AbstractTodoDemoIT {

    // FIXME: change expected log when https://github.com/quarkus-qe/quarkus-test-framework/issues/1183 is fixed
    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "Future log output will appear in directory")
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
