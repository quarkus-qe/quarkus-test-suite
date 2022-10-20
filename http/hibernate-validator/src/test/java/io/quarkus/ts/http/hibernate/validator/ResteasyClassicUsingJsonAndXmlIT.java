package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.ClassicResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class ResteasyClassicUsingJsonAndXmlIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ClassicResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-resteasy-jackson"),
            @Dependency(artifactId = "quarkus-resteasy-jaxb")
    })
    static final RestService app = new RestService();

    /**
     * When JSON and XML library are present, default is JSON
     */
    @Test
    public void validateDefaultMediaType() {
        validate(CLASSIC_ENDPOINT_WITH_NO_PRODUCES)
                .isBadRequest()
                .hasClassicJsonError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptXml() {
        Response response = given().accept(ContentType.XML).get(CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasClassicXMLError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptJson() {
        Response response = given().accept(ContentType.JSON).get(CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasClassicJsonError();
    }

    @Test
    public void validateMultipleMediaTypesUsingAcceptText() {
        Response response = given().accept(ContentType.TEXT).get(CLASSIC_ENDPOINT_WITH_MULTIPLE_PRODUCES);
        validate(response)
                .isBadRequest()
                .hasTextError();
    }
}
