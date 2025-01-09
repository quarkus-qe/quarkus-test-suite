package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.restassured.specification.RequestSpecification;

@Tag("use-quarkus-openshift-extension")
@Tag("serverless")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtensionAndDockerBuildStrategy)
public class ServerlessExtensionDockerBuildStrategyOpenShiftHttpMinimumReactiveIT extends HttpMinimumReactiveIT {
    private RequestSpecification HTTPS_CLIENT_SPEC = given().relaxedHTTPSValidation();

    @Override
    protected RequestSpecification givenSpec() {
        return HTTPS_CLIENT_SPEC;
    }

    @Override
    @Disabled("Serverless deployments do not have logs which we can check")
    void interceptedMethodFound() {
    }
}
