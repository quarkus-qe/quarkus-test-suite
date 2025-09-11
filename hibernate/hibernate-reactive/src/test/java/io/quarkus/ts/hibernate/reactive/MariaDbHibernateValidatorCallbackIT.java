package io.quarkus.ts.hibernate.reactive;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6242")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
@QuarkusScenario
public class MariaDbHibernateValidatorCallbackIT extends AbstractHibernateValidatorIT {

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.1011.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl)
            .withProperty("quarkus.hibernate-orm.validation.mode", "callback");

    @Test
    @Override
    public void validationInsertNullValue() {
        String responseBody = getApp().given().get("/validation/insert-null-value")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();
        assertEquals("Column allows NULL values", responseBody);
    }

    @Test
    @Override
    public void validationPersonSchema() {
        getApp().given().get("/validation/schema/person")
                .then()
                .statusCode(HttpStatus.SC_OK)
                // DDL checks are disabled so any constraints defined in the entity are not applied to the table
                .body("notNullApplied", equalTo(false))
                .body("correctMaxLength", equalTo(false));
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
