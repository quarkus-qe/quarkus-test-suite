package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ClassicResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@DisabledOnNative(reason = "Due to high native build execution time")
@QuarkusScenario
public class ResteasyClassicUsingXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ClassicResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-jaxb")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        // The default media type is TEXT which looks wrong for me, but it's how has been designed following the TCK standard.
        assertBadRequestInTextFormat(CLASSIC_ENDPOINT_WITH_NO_PRODUCES);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptXml() {
        Response response = given().accept(ContentType.XML).get(CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInXmlFormat(response);
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        assertBadRequestInTextFormat(response);
    }
}
