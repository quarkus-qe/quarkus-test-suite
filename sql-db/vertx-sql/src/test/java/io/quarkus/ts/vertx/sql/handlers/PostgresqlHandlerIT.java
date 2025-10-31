package io.quarkus.ts.vertx.sql.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class PostgresqlHandlerIT extends CommonTestCases {
    private static final String POSTGRESQL_DATABASE = "amadeus";

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgres = new PostgresqlService()
            .with("test", "test", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", postgres.getUser())
            .withProperty("quarkus.datasource.password", postgres.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", postgres::getJdbcUrl)
            .withProperty("quarkus.datasource.reactive.url", postgres::getReactiveUrl)
            .withProperty("app.selected.db", "postgresql")
            // Enable Flyway for Postgresql
            .withProperty("quarkus.flyway.migrate-at-start", "true");

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/50140")
    @DisabledOnQuarkusVersion(version = "3.27.0.*", reason = "This issue affected native and was fixed in 3.27.1")
    void manuallyCreatedPool() {
        Response response = app.given().get("pool/");
        assertEquals(200, response.statusCode());
        assertEquals("Total cities: 9", response.body().asString());
    }
}
