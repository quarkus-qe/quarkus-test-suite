package io.quarkus.ts.hibernate.reactive;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-6242")
public class HibernateValidatorDisabledIT {

    private static final String POSTGRES_USER = "quarkus_test";
    private static final String POSTGRES_PASSWORD = "quarkus_test";
    private static final String POSTGRES_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            .withUser(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withDatabase(POSTGRES_DATABASE)
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", POSTGRES_USER)
            .withProperty("quarkus.datasource.password", POSTGRES_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl)
            .withProperty("quarkus.hibernate-orm.validation.mode", "none");

    @Test
    public void validationCreatePersonWithValidName() {
        getApp().given().put("/validation/person/Alex")
                .then()
                .statusCode(SC_CREATED);
    }

    @Test
    public void validationSuccessCreatePersonWithInvalidName() {
        getApp().given().put("/validation/person/InvalidNameWithMoreThanThirtyChars").then()
                .statusCode(SC_CREATED);
    }

    @Test
    public void validationInsertNullValue() {
        String responseBody = getApp().given().get("/validation/insert-null-value")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();
        assertEquals("Column allows NULL values", responseBody);
    }

    public RestService getApp() {
        return app;
    }
}
