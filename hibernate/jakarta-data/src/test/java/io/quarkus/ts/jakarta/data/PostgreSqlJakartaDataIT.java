package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/2631")
public class PostgreSqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService();

    @QuarkusApplication
    static final RestService app = createApp(database, "pg");
}
