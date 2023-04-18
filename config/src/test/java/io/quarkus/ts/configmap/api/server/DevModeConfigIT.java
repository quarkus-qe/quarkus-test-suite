package io.quarkus.ts.configmap.api.server;

import static io.quarkus.ts.configmap.api.server.PropertiesSource.APP_PROPERTIES;
import static io.quarkus.ts.configmap.api.server.PropertiesSource.INJECTED_PROPERTIES;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.BASE64;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.CRYPTO_AES_GCM_NO_PADDING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@DisabledOnOs(value = WINDOWS, disabledReason = "Needs Docker support") // Windows containers are not supported by TestContainers
@QuarkusScenario
public class DevModeConfigIT {

    private static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";
    private static final String CHANGED = "changed";

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("quarkus.profile", "dev,profile1,profile2,profile3");

    @BeforeAll
    public static void beforeAll() {
        PropertiesSource.given = app::given;
    }

    @Order(1)
    @Test
    void configProfileDependentSecret() {
        // assert config properties only available with certain config profiles
        assertProfileSecret("dev");
        assertProfileSecret("profile1");
        assertProfileSecret("profile2");

        // assert profile config property overrides property also set
        // without '%profile3.' prefix; that is profile has priority
        var secretKey = CRYPTO_AES_GCM_NO_PADDING.secretKey(APP_PROPERTIES);
        var secretVal = "profile3";
        INJECTED_PROPERTIES.assertSecret(secretKey, secretVal);
    }

    @Order(2)
    @Test
    void changeSecretInRunningApp() {
        var secretKey = BASE64.secretKey(APP_PROPERTIES);
        var expectedSecret = BASE64.secret;
        INJECTED_PROPERTIES.assertSecret(secretKey, expectedSecret);

        var newSecretKey = new String(Base64.getEncoder().encode(CHANGED.getBytes(UTF_8)), UTF_8);
        var oldSecretKey = new String(Base64.getEncoder().encode(expectedSecret.getBytes(UTF_8)), UTF_8);
        app.modifyFile(APPLICATION_PROPERTIES, props -> props.replace(oldSecretKey, newSecretKey));
        await().atMost(ofSeconds(20)).untilAsserted(() -> INJECTED_PROPERTIES.assertSecret(secretKey, CHANGED));
    }

    private static void assertProfileSecret(String profile) {
        var secretKey = CRYPTO_AES_GCM_NO_PADDING.secretKey(APP_PROPERTIES, profile);
        INJECTED_PROPERTIES.assertSecret(secretKey, profile);
    }

}
