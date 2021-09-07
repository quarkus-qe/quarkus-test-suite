package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class Db2HandlerIT extends CommonTestCases {
    private static final String DATABASE = "amadeus";

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static DefaultService db2 = new DefaultService()
            .withProperty("LICENSE", "accept")
            .withProperty("DB2INST1_PASSWORD", "test")
            .withProperty("DB2INSTANCE", "test")
            .withProperty("AUTOCONFIG", "false")
            .withProperty("ARCHIVE_LOGS", "false")
            .withProperty("DBNAME", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db2.jdbc.url",
                    () -> db2.getHost().replace("http", "jdbc:db2") + ":" +
                            db2.getPort() + "/" + DATABASE)
            .withProperty("quarkus.datasource.db2.reactive.url",
                    () -> db2.getHost().replace("http", "db2") + ":" +
                            db2.getPort() + "/" + DATABASE)
            .withProperty("app.selected.db", "db2")
            // Enable Flyway for DB2
            .withProperty("quarkus.flyway.db2.migrate-at-start", "true");

    @Override
    public RestService app() {
        return app;
    }
}
