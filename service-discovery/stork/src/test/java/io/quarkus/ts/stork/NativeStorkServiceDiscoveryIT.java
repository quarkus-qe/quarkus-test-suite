package io.quarkus.ts.stork;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.ConsulService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class NativeStorkServiceDiscoveryIT extends AbstractCommonTestCases {
    private static final String PUNG_PORT = getAvailablePort();

    @Container(image = "${consul.image}", expectedLog = "Synced node info", port = 8500)
    static ConsulService consul = new ConsulService();

    @QuarkusApplication(classes = PungResource.class)
    static RestService pungService = new RestService()
            .withProperty("quarkus.http.port", PUNG_PORT)
            .withProperty("pung-service-port", PUNG_PORT)
            .withProperty("pung-service-host", "localhost")
            .withProperty("quarkus.stork.pung.service-discovery.type", "consul")
            .withProperty("quarkus.stork.pung.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("quarkus.stork.pung.service-discovery.consul-host",
                    () -> getConsultEndpoint(consul.getConsulEndpoint()));

    @QuarkusApplication(classes = { PingResource.class, MyBackendPungProxy.class, MyBackendPongProxy.class })
    static RestService pingService = new RestService()
            .withProperty("quarkus.stork.pung.service-discovery.type", "consul")
            .withProperty("quarkus.stork.pung.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("quarkus.stork.pung.service-discovery.consul-host",
                    () -> getConsultEndpoint(consul.getConsulEndpoint()));

    @Test
    public void invokeServiceByName() {
        String response = makePingCall(pingService, "pung").extract().body().asString();
        assertThat("Service discovery by name fail.", PREFIX + "pung", is(equalToIgnoringCase(response)));
    }
}
