package io.quarkus.ts.http.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class RestClientIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @QuarkusApplication
    static RestService appWithMicrometerDisabled = new RestService().withProperty("quarkus.micrometer.enabled", "false");

    @Test
    public void shouldGetBookFromRestClientXml() {
        app.given().get("/client/book/xml").then().statusCode(HttpStatus.SC_OK)
                .body(is(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml</title></book>"));
    }

    @Test
    public void shouldGetBookFromRestClientJson() {
        app.given().get("/client/book/json").then().statusCode(HttpStatus.SC_OK)
                .body(is("{\"title\":\"Title in Json\"}"));
    }

    @Test
    @Tag("QUARKUS-1376")
    public void notFoundShouldNotReturnAnyResteasyImplementationDetails() {
        String body = app.given().get("/notFound").then().statusCode(HttpStatus.SC_NOT_FOUND).extract().body().asString();
        Assertions.assertFalse(body.contains("RESTEASY"),
                "Not found resource should not return any Resteasy implementation details, but was: " + body);
    }

    @Test
    @Tag("QUARKUS-2127")
    public void noNullPointerExceptionForRestClientUsageWithDisabledMicrometer() {
        appWithMicrometerDisabled.given().get("/client/book/xml").then().statusCode(HttpStatus.SC_OK);
    }
}
