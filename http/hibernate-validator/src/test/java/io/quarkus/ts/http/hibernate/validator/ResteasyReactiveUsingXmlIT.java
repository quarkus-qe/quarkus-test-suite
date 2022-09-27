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

@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyReactiveUsingXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-reactive-jaxb")
    })
    static final RestService app = new RestService();

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/28324")
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
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(REACTIVE_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasTextError();
    }
}
