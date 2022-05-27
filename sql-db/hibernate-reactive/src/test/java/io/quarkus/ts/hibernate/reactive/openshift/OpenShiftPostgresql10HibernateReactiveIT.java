package io.quarkus.ts.hibernate.reactive.openshift;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.AbstractDatabaseHibernateReactiveIT;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftPostgresql10HibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String POSTGRES_USER = "quarkus_test";
    private static final String POSTGRES_PASSWORD = "quarkus_test";
    private static final String POSTGRES_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;

    @Container(image = "${postgresql.10.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            .withUser(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withDatabase(POSTGRES_DATABASE);

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
