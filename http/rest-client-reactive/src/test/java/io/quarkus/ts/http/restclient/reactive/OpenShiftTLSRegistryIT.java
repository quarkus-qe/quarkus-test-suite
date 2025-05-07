package io.quarkus.ts.http.restclient.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-5664")
@QuarkusScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1052")
//some additional changes may be needed even if the issue above is fixed (eg host verification)
public class OpenShiftTLSRegistryIT extends TLSRegistryIT {
}
