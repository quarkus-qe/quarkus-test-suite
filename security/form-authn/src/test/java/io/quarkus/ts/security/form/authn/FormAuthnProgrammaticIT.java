package io.quarkus.ts.security.form.authn;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class FormAuthnProgrammaticIT extends FormAuthnBase {
    @QuarkusApplication(classes = { UserResource.class, FormAuthnProgrammaticConfiguration.class })
    static RestService app = new RestService();
}
