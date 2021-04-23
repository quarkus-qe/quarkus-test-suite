package io.quarkus.ts.http.advanced;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@EnabledIfOpenShiftScenarioPropertyIsTrue
@OpenShiftScenario
public class OpenShiftHttpAdvancedIT extends HttpAdvancedIT {
    @Override
    protected Protocol getProtocol() {
        // HTTPs is not supported in OpenShift yet. The same happens in OpenShift TS.
        return Protocol.HTTP;
    }
}
