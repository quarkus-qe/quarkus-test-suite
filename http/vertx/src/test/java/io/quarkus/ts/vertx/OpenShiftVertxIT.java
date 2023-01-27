package io.quarkus.ts.vertx;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@OpenShiftScenario
public class OpenShiftVertxIT extends AbstractVertxIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Override
    public RequestSpecification requests() {
        return app.given();
    }
}
