package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/2631")
public class OpenShiftPostgreSqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static final RestService app = createApp(database, "pg");

}
