package io.quarkus.ts.http.restclient.reactive;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@Tag("QUARKUS-5664")
@QuarkusScenario
@DisabledOnNative(reason = "Enable this when we using RHEL9 as runners")
//some additional changes may be needed even if the issue above is fixed (eg host verification)
public class OpenShiftTLSRegistryIT extends TLSRegistryIT {
}
