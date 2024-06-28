package io.quarkus.ts.hibernate.reactive.openshift;

import static io.quarkus.ts.hibernate.reactive.PostgresqlDatabaseHibernateReactiveIT.POSTGRES_DATABASE;
import static io.quarkus.ts.hibernate.reactive.PostgresqlDatabaseHibernateReactiveIT.POSTGRES_PASSWORD;
import static io.quarkus.ts.hibernate.reactive.PostgresqlDatabaseHibernateReactiveIT.POSTGRES_PORT;
import static io.quarkus.ts.hibernate.reactive.PostgresqlDatabaseHibernateReactiveIT.POSTGRES_USER;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.AbstractDatabaseHibernateReactiveIT;

@OpenShiftScenario
public class OpenShiftPostgresqlHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

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
