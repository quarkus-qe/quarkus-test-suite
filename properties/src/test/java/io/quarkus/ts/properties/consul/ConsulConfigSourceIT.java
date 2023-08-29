package io.quarkus.ts.properties.consul;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.ConsulService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ConsulConfigSourceIT {

    private static final String CONSUL_KEY = "config/app";
    private static final String CUSTOM_PROPERTY = "welcome.message";

    @Container(image = "${consul.image}", expectedLog = "Synced node info", port = 8500)
    static ConsulService consul = new ConsulService().onPostStart(ConsulConfigSourceIT::onLoadConfigureConsul);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.consul-config.enabled", "true")
            .withProperty("quarkus.consul-config.properties-value-keys", CONSUL_KEY)
            .withProperty("quarkus.consul-config.agent.host-port", () -> consul.getConsulEndpoint());

    @Test
    public void shouldUpdateCustomProperty() {
        thenGreetingsApiReturns("Hello Default");

        whenUpdateCustomPropertyTo("Hello Test");
        thenGreetingsApiReturns("Hello Test");
    }

    protected static final void onLoadConfigureConsul(Service service) {
        loadPropertiesWithCustomPropertyTo("Hello Default");
    }

    private void whenUpdateCustomPropertyTo(String newValue) {
        loadPropertiesWithCustomPropertyTo(newValue);

        app.stop();
        app.start();
    }

    private void thenGreetingsApiReturns(String expected) {
        untilAsserted(() -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK).extract().asString(),
                actual -> assertEquals(expected, actual, "Unexpected response from service"));
    }

    private static void loadPropertiesWithCustomPropertyTo(String value) {
        try {
            String propertiesContent = IOUtils.toString(ConsulConfigSourceIT.class.getClassLoader()
                    .getResourceAsStream("application.properties"), StandardCharsets.UTF_8);
            propertiesContent += "\n" + CUSTOM_PROPERTY + "=" + value;
            consul.loadPropertiesFromString(CONSUL_KEY, propertiesContent);
        } catch (IOException e) {
            Assertions.fail("Failed to load application.properties");
        }
    }
}
