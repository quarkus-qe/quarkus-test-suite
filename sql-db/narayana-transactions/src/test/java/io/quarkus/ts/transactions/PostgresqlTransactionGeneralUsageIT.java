package io.quarkus.ts.transactions;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlTransactionGeneralUsageIT extends AbstractPostgresqlTransactionGeneralUsageIT {

    private static final String ENABLE_PREPARED_TRANSACTIONS = "--max_prepared_transactions=100";

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address", command = ENABLE_PREPARED_TRANSACTIONS)
    static final PostgresqlService database = new PostgresqlService().withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    public static final RestService app = createQuarkusApp(database);

    @Override
    protected RestService getApp() {
        return app;
    }

}
