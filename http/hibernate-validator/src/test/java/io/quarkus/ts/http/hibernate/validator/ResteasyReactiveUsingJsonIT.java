package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;

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
public class ResteasyReactiveUsingJsonIT extends BaseResteasyIT {

    @QuarkusApplication(classes = ReactiveResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-rest-jackson")
    })
    static final RestService app = new RestService();

    @Test
    public void validateDefaultMediaType() {
        validate(REACTIVE_ENDPOINT_WITH_NO_PRODUCES)
                .isBadRequest()
                .hasTextError();
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
}
