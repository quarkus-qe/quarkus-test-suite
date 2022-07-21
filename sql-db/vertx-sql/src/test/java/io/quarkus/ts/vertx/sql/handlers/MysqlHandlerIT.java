package io.quarkus.ts.vertx.sql.handlers;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO https://github.com/quarkus-qe/quarkus-test-suite/issues/756
@Tag("fips-incompatible") // native-mode
@QuarkusScenario
public class MysqlHandlerIT extends CommonTestCases {
    private static final String DATABASE = "amadeus";

    @Container(image = "${mysql.80.image}", port = 3306, expectedLog = "Only MySQL server logs after this point")
    static MySqlService mysql = new MySqlService()
            .with("test", "test", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mysql.username", mysql.getUser())
            .withProperty("quarkus.datasource.mysql.password", mysql.getPassword())
            .withProperty("quarkus.datasource.mysql.jdbc.url", mysql::getJdbcUrl)
            .withProperty("quarkus.datasource.mysql.reactive.url", mysql::getReactiveUrl)
            .withProperty("app.selected.db", "mysql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "true");
}
