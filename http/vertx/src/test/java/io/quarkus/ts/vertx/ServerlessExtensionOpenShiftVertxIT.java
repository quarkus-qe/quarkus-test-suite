package io.quarkus.ts.vertx;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@Tag("use-quarkus-openshift-extension")
@Tag("serverless")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class ServerlessExtensionOpenShiftVertxIT extends AbstractVertxIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Override
    public RequestSpecification requests() {
        return app.given().relaxedHTTPSValidation();
    }
}
