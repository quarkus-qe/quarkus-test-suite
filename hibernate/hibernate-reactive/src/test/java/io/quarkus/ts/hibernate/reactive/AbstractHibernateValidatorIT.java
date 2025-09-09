package io.quarkus.ts.hibernate.reactive;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;

@Tag("QUARKUS-6242")
public abstract class AbstractHibernateValidatorIT {

    @Test
    public void validationCreatePersonWithValidName() {
        getApp().given().put("/validation/person/Alex")
                .then()
                .statusCode(SC_CREATED);
    }

    @Test
    public void validationCreatePersonWithInvalidName() {
        getApp().given().put("/validation/person/InvalidNameWithMoreThanThirtyChars").then()
                .statusCode(SC_BAD_REQUEST)
                .body(containsString("Name must not exceed 30 characters"));
    }

    @Test
    public void validationInsertNullValue() {
        String responseBody = getApp().given().get("/validation/insert-null-value")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals("NOT NULL constraint enforced", responseBody);
    }

    @Test
    public void validationPersonSchema() {
        getApp().given().get("/validation/schema/person")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("notNullApplied", equalTo(true))
                .body("correctMaxLength", equalTo(true));
    }

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlInvalidCustomerEmail() {
        getApp().given()
                .when().put("/validation/xml/customer/Vick/not-an-email")
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body(containsString("XML constraint violation"));
    }

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlInvalidCustomerName() {
        getApp().given()
                .when().put("/validation/xml/customer/No/customer@example.com")
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body(containsString("XML constraint violation"));
    }

    @Tag("QUARKUS-6262")
    @Test
    public void validationXmlValidCustomer() {
        getApp().given()
                .when().put("/validation/xml/customer/Bjorn/brojn@example.com")
                .then()
                .statusCode(SC_CREATED);
    }

    protected abstract RestService getApp();
}
