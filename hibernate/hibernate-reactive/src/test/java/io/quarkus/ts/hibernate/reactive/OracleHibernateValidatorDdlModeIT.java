package io.quarkus.ts.hibernate.reactive;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6242")
@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
public class OracleHibernateValidatorDdlModeIT extends AbstractHibernateValidatorIT {

    private static final String ORACLE_USER = "quarkus_test";
    private static final String ORACLE_PASSWORD = "quarkus_test";
    private static final String ORACLE_DATABASE = "quarkus_test";
    private static final int ORACLE_PORT = 1521;

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService database = new OracleService()
            .with(ORACLE_USER, ORACLE_PASSWORD, ORACLE_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.hibernate-orm.schema-management.strategy", "create")
            .withProperty("quarkus.datasource.db-kind", "oracle")
            .withProperty("quarkus.datasource.username", ORACLE_USER)
            .withProperty("quarkus.datasource.password", ORACLE_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl)
            .withProperty("quarkus.hibernate-orm.validation.mode", "ddl");

    @Test
    @Override
    public void validationCreatePersonWithInvalidName() {
        getApp().given().put("/validation/person/InvalidNameWithMoreThanThirtyChars").then()
                // DDL mode applies constraints to table, but error is thrown on Oracle side, not Hibernate
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @Override
    public void validationXmlInvalidCustomerEmail() {
        getApp().given()
                .when().put("/validation/xml/customer/Al/not-an-email")
                .then()
                .statusCode(SC_CREATED);
    }

    @Test
    @Override
    public void validationXmlInvalidCustomerName() {
        getApp().given()
                .when().put("/validation/xml/customer/No/customer@example.com")
                .then()
                .statusCode(SC_CREATED);
    }

    public RestService getApp() {
        return app;
    }
}
