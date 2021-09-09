package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class Db2HandlerIT extends CommonTestCases {
    private static final String DATABASE = "amadeus";

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static Db2Service db2 = new Db2Service()
            .with("test", "test", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db2.jdbc.url", db2::getJdbcUrl)
            .withProperty("quarkus.datasource.db2.reactive.url", db2::getReactiveUrl)
            .withProperty("app.selected.db", "db2")
            // Enable Flyway for DB2
            .withProperty("quarkus.flyway.db2.migrate-at-start", "true");

    @Override
    public RestService app() {
        return app;
    }
}
