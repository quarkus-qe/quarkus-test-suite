package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ReactiveResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class ResteasyReactiveUsingJsonAndXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive-jackson"),
            @Dependency(artifactId = "quarkus-resteasy-reactive-jaxb")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        validate(REACTIVE_ENDPOINT_WITH_NO_PRODUCES)
                .isBadRequest()
                .hasTextError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptXml() {
        Response response = given().accept(ContentType.XML).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasReactiveXMLError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptJson() {
        Response response = given().accept(ContentType.JSON).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasReactiveJsonError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasTextError();
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/28421")
    public void validateReturnValue() {
        Response response = given()
                .accept(ContentType.TEXT)
                .get("/reactive/validate-response-uni/mouse");
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, //https://github.com/quarkusio/quarkus/issues/28422
                response.statusCode());
        response.then().body(containsString("response must have 3 characters"));
    }
}
