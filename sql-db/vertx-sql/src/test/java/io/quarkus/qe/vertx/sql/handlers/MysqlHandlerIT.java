package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MysqlHandlerIT extends CommonTestCases {
    private static final String DATABASE = "amadeus";

    @Container(image = "${mysql.57.image}", port = 3306, expectedLog = "ready for connections")
    static DefaultService mysql = new DefaultService()
            .withProperty("MYSQL_ROOT_PASSWORD", "test")
            .withProperty("MYSQL_USER", "test")
            .withProperty("MYSQL_PASSWORD", "test")
            .withProperty("MYSQL_DATABASE", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mysql.jdbc.url",
                    () -> mysql.getHost().replace("http", "jdbc:mysql") + ":" +
                            mysql.getPort() + "/" + DATABASE)
            .withProperty("quarkus.datasource.mysql.reactive.url",
                    () -> mysql.getHost().replace("http", "mysql") + ":" +
                            mysql.getPort() + "/" + DATABASE)
            .withProperty("app.selected.db", "mysql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "true");

    @Override
    public RestService app() {
        return app;
    }
}
