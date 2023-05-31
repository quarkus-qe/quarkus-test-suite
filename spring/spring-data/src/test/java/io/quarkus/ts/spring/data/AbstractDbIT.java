package io.quarkus.ts.spring.data;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public class AbstractDbIT {
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

}
