package io.quarkus.qe.hibernate;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ProdHibernateIT extends BaseHibernateIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.13.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService();

    @QuarkusApplication
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
