package io.quarkus.ts.hibernate.reactive.openshift;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.AbstractDatabaseHibernateReactiveIT;

@OpenShiftScenario
public class OpenShiftPostgresqlHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String POSTGRES_USER = "quarkus_test";
    private static final String POSTGRES_PASSWORD = "quarkus_test";
    private static final String POSTGRES_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;

    // FIXME: change expected log when https://github.com/quarkus-qe/quarkus-test-framework/issues/1183 is fixed
    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "Future log output will appear in directory")
    static PostgresqlService database = new PostgresqlService()
            .withUser(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withDatabase(POSTGRES_DATABASE)
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", POSTGRES_USER)
            .withProperty("quarkus.datasource.password", POSTGRES_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

}
