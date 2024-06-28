package io.quarkus.ts.reactive.rest.data.panache;

import static io.quarkus.ts.reactive.rest.data.panache.PostgresqlPanacheResourceIT.POSTGRESQL_PORT;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftPostgresqlPanacheResourceIT extends AbstractPanacheResourceIT {

    // FIXME: change expected log when https://github.com/quarkus-qe/quarkus-test-framework/issues/1183 is fixed
    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "Future log output will appear in directory")
    public static final PostgresqlService database = new PostgresqlService()
            .withProperty("POSTGRES_DB", "mydb")
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    public static final RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

}
