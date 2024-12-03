package io.quarkus.ts.http.advanced.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftHeadersIT extends HeadersIT {

    @Test
    @Override
    @Disabled("This test won't work on Openshift as the request expect valid `Host` header and this test check the http/1.0 without `Host` header")
    public void testHttp10WithoutHostHeader() {
    }
}
