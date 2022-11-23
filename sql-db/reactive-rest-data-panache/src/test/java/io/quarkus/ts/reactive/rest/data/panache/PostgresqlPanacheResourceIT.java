package io.quarkus.ts.reactive.rest.data.panache;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlPanacheResourceIT extends AbstractPanacheResourceIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    public static final PostgresqlService database = new PostgresqlService()
            .withProperty("POSTGRES_DB", "mydb")
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    public static final RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
