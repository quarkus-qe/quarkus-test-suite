package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ReactiveResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Disabled("Wrong MediaType resolution in Resteasy Reactive: https://github.com/quarkusio/quarkus/issues/20888")
@QuarkusScenario
public class ResteasyReactiveUsingJsonAndXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive-jackson"),
            @Dependency(artifactId = "quarkus-resteasy-reactive-jaxb")
    })
    static final RestService app = new RestService();

    /**
     * When JSON and XML library are present, default is JSON
     */
    @Test
    public void validateDefaultMediaType() {
        assertBadRequestInJsonFormat(REACTIVE_ENDPOINT_WITH_NO_PRODUCES);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptXml() {
        Response response = given().accept(ContentType.XML).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInXmlFormat(response);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptJson() {
        Response response = given().accept(ContentType.JSON).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInJsonFormat(response);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInTextFormat(response);
    }
}
