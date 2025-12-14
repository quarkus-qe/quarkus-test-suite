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

    @Tag("QUARKUS-6968")
    @Test
    public void validationCyclicCascadeValidBook() {
        getApp().given()
                .put("/validation/author/Millman/validBook")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationCyclicCascadeInvalidBook() {
        getApp().given()
                .put("/validation/author/Unknown/No")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationValidIPv4Address() {
        getApp().given()
                .put("/validation/device/ipv4/10.102.103.104")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationRejectIPv6ForIPv4Field() {
        getApp().given()
                .put("/validation/device/ipv4/2001:db8::1")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationAcceptIpv6Address() {
        getApp().given()
                .put("/validation/device/ipv6/2001:db8:1::ab9:C0A8:102")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationRejectIpv4ForIpv6Field() {
        getApp().given()
                .put("/validation/device/ipv6/1.2.3.4")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationLegacyContainerInvalid() {
        getApp().given()
                .put("/validation/container/legacy/AB")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("books[0].title"));
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationTypeUseContainerInvalid() {
        getApp().given()
                .put("/validation/container/typeuse/AB")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("books[0].title"));
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationTypeUseContainerValid() {
        getApp().given()
                .put("/validation/container/typeuse/ValidTitle")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Tag("QUARKUS-6968")
    @Test
    public void validationLegacyContainerValid() {
        getApp().given()
                .put("/validation/container/legacy/ValidTitle")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    protected abstract RestService getApp();
}
