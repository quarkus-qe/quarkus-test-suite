package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MysqlHandlerIT extends CommonTestCases {
    private static final String DATABASE = "amadeus";

    @Container(image = "${mysql.57.image}", port = 3306, expectedLog = "ready for connections")
    static MySqlService mysql = new MySqlService()
            .with("test", "test", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mysql.jdbc.url", mysql::getJdbcUrl)
            .withProperty("quarkus.datasource.mysql.reactive.url", mysql::getReactiveUrl)
            .withProperty("app.selected.db", "mysql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "true");
}
