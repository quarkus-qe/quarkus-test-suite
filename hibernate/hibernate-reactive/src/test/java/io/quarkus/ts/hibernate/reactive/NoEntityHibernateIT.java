package io.quarkus.ts.hibernate.reactive;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.entityless.EntitylessEndpoint;

@QuarkusScenario
@Tag("QUARKUS-6965")
public class NoEntityHibernateIT {
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService();

    @QuarkusApplication(classes = {
            EntitylessEndpoint.class }, includeAllClassesFromMain = true, properties = "entityless.properties")
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Test
    public void nativeQueryGetTest() {
        app.given()
                .param("name", "Harry")
                .get("/entityless/native-query")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Wizard"));
    }
}
