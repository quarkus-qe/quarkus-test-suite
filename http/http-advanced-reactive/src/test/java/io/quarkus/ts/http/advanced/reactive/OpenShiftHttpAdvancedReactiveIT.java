package io.quarkus.ts.http.advanced.reactive;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftHttpAdvancedReactiveIT extends HttpAdvancedReactiveIT {
    @Override
    protected Protocol getProtocol() {
        // HTTPs is not supported in OpenShift yet. The same happens in OpenShift TS.
        return Protocol.HTTP;
    }
}
