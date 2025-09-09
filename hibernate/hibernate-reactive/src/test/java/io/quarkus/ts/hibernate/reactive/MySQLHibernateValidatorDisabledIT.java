package io.quarkus.ts.hibernate.reactive;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("fips-incompatible") // TODO: enable when the https://github.com/eclipse-vertx/vertx-sql-client/issues/1436 is fixed
@Tag("QUARKUS-6242")
@QuarkusScenario
public class MySQLHibernateValidatorDisabledIT {

    private static final String MYSQL_USER = "quarkus_test";
    private static final String MYSQL_PASSWORD = "quarkus_test";
    private static final String MYSQL_DATABASE = "quarkus_test";
    private static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService()
            .withUser(MYSQL_USER)
            .withPassword(MYSQL_PASSWORD)
            .withDatabase(MYSQL_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", MYSQL_USER)
            .withProperty("quarkus.datasource.password", MYSQL_PASSWORD)
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

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlAcceptInvalidCustomerEmail() {
        getApp().given()
                .when().put("/validation/xml/customer/Vick/not-an-email")
                .then()
                .statusCode(SC_CREATED);
    }

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlAcceptInvalidCustomerName() {
        getApp().given()
                .when().put("/validation/xml/customer/No/customer@example.com")
                .then()
                .statusCode(SC_CREATED);
    }

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlValidCustomer() {
        getApp().given()
                .when().put("/validation/xml/customer/Bjorn/brojn@example.com")
                .then()
                .statusCode(SC_CREATED);
    }

    public RestService getApp() {
        return app;
    }
}
