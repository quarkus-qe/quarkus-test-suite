package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ReactiveResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Disabled("Wrong MediaType resolution in Resteasy Reactive: https://github.com/quarkusio/quarkus/issues/20888")
@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyReactiveUsingXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive-jaxb")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        // The default media type is TEXT which looks wrong for me, but it's how has been designed following the TCK standard.
        assertBadRequestInTextFormat(REACTIVE_ENDPOINT_WITH_NO_PRODUCES);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptXml() {
        Response response = given().accept(ContentType.XML).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInXmlFormat(response);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInTextFormat(response);
    }
}
