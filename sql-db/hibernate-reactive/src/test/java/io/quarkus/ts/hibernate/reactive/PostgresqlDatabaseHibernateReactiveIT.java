package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String POSTGRES_USER = "quarkus_test";
    private static final String POSTGRES_PASSWORD = "quarkus_test";
    private static final String POSTGRES_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
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

    // TODO: drop is overridden method when #40425 is fixed
    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/40425")
    @Override
    public void createBookWithGeneratedId() {
        super.createBookWithGeneratedId();
    }

    // TODO: drop is overridden method when #40425 is fixed
    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/40425")
    @Override
    public void useTransaction() {
        super.useTransaction();
    }

    // TODO: drop is overridden method when #40425 is fixed
    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/40425")
    @Override
    public void ensureSessionIsPropagatedOnReactiveTransactions() {
        super.ensureSessionIsPropagatedOnReactiveTransactions();
    }
}
