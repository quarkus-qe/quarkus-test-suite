package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.restassured.specification.RequestSpecification;

@Tag("use-quarkus-openshift-extension")
@Tag("serverless")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
// TODO https://github.com/quarkusio/quarkus/issues/29921
@DisabledOnQuarkusSnapshot(reason = "OpenShift extension issue on docker image pushing")
public class ServerlessExtensionOpenShiftHttpMinimumReactiveIT extends HttpMinimumReactiveIT {

    private RequestSpecification HTTPS_CLIENT_SPEC = given().relaxedHTTPSValidation();

    @Override
    protected RequestSpecification givenSpec() {
        return HTTPS_CLIENT_SPEC;
    }
}