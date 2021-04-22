package io.quarkus.ts.security.jwt;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftOauth2JwtSecurityIT extends Oauth2JwtSecurityIT {
}
