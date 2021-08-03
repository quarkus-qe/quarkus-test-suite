package io.quarkus.qe.properties.consul;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.ConsulService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "TODO: Caused by https://github.com/quarkus-qe/quarkus-test-framework/issues/169")
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support Linux Containers / Testcontainers")
public class ConsulConfigSourceIT {

    private static final String CUSTOM_PROPERTY = "welcome.message";

    @Container(image = "quay.io/bitnami/consul:1.9.3", expectedLog = "Synced node info", port = 8500)
    static ConsulService consul = new ConsulService().onPostStart(ConsulConfigSourceIT::onLoadConfigureConsul);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.consul-config.enabled", "true")
            .withProperty("quarkus.consul-config.properties-value-keys", "config/app")
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
            consul.loadPropertiesFromString(propertiesContent);
        } catch (IOException e) {
            Assertions.fail("Failed to load application.properties");
        }
    }
}
