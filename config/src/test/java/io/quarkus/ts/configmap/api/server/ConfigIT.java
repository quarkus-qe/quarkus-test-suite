package io.quarkus.ts.configmap.api.server;

import static io.quarkus.ts.configmap.api.server.PropertiesSource.APP_PROPERTIES;
import static io.quarkus.ts.configmap.api.server.PropertiesSource.BUILT_CONFIG;
import static io.quarkus.ts.configmap.api.server.PropertiesSource.INJECTED_CONFIG;
import static io.quarkus.ts.configmap.api.server.PropertiesSource.INJECTED_PROPERTIES;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.BASE64;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.CRYPTO_AES_GCM_NO_PADDING;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.RSA;
import static io.quarkus.ts.configmap.api.server.SecretKeysHandler.SHA256;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ConfigIT {

    private static final String ANSWER_KEY = "the.answer";
    private static final String ANSWER_VALUE = "42";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty(ANSWER_KEY, ANSWER_VALUE)
            .withProperty("secret.password", "T0tallySafePa\\$\\$word")
            .withProperty("secret.ip", "127.0.0.1")
            .withProperty("secrets.system-crypto-handler",
                    "${aes-gcm-nopadding::DEaZok2mA76F-jak70kWav7Gx65QarcWbul-bvLgCHzy9eiHMkCWdadFISES2H7lewF61Ct-jqNaSQ}")
            .withProperty("secrets.system-base64-handler", "${custom-base64::cXVhcmt1cy1xZS1iYXNlNjQ=}")
            .withProperty("secrets.system-sha256-handler",
                    "${sha256::0e8c2a8b8ecfbd52c3ef17acd44498ee2b892c66b308598f6a88ca8c7a235c4e}");

    @BeforeAll
    public static void beforeAll() {
        PropertiesSource.given = app::given;
    }

    @Test
    void injectedProperties() {
        // when we inject properties directly, they are not marked as secret
        INJECTED_PROPERTIES.assertSecret(ANSWER_KEY, ANSWER_VALUE);
        INJECTED_PROPERTIES.assertSecret("secret.password", "T0tallySafePa$$word");
        INJECTED_PROPERTIES.assertSecret("secret.ip", "127.0.0.1");
    }

    @Test
    void injectedConfig() {
        // when we inject config, properties are marked as secret by interceptor
        INJECTED_CONFIG.assertSecret(ANSWER_KEY, ANSWER_VALUE);
        INJECTED_CONFIG.assertAccessDenied("secret.password");
        INJECTED_CONFIG.unlocked().assertSecret("secret.password", "T0tallySafePa$$word");
        INJECTED_CONFIG.assertSecret("secret.ip", "127.0.0.1");
    }

    @Test
    void builtConfig() {
        // when we build properties, we choose, which ones we mark as secret
        BUILT_CONFIG.assertSecret(ANSWER_KEY, ANSWER_VALUE);
        BUILT_CONFIG.unlocked().assertSecret("secret.password", "T0tallySafePa$$word");
        BUILT_CONFIG.assertAccessDenied("secret.ip");
        BUILT_CONFIG.unlocked().assertSecret("secret.ip", "127.0.0.1");
    }

    @Test
    void unlocked() {
        // verify, what doUnlock doesn't mess with public values
        INJECTED_PROPERTIES.unlocked().assertSecret(ANSWER_KEY, ANSWER_VALUE);
        INJECTED_CONFIG.unlocked().assertSecret(ANSWER_KEY, ANSWER_VALUE);
        BUILT_CONFIG.unlocked().assertSecret(ANSWER_KEY, ANSWER_VALUE);
    }

    @Test
    void keyStoreConfigSource() {
        var keystoreConfigKey = "plain-keystore-config-key";
        var keystoreConfigValue = "plain-keystore-config-value";
        INJECTED_PROPERTIES.assertSecret(keystoreConfigKey, keystoreConfigValue);
        INJECTED_CONFIG.assertSecret(keystoreConfigKey, keystoreConfigValue);
        BUILT_CONFIG.assertSecret(keystoreConfigKey, keystoreConfigValue);
    }

    @Test
    void customSecretKeysHandler() {
        // handler registered via `ServiceLoader` (through `SecretKeysHandler` service)
        testSecretKeysHandler(BASE64);

        // handler registered via `ServiceLoader` (through `SecretKeysHandlerFactory` service)
        testSecretKeysHandler(SHA256);

        // handler registered against manually built config
        // as existence of handlers is validated at build time when the handler is not yet registered
        // we can only test it against manually built config
        BUILT_CONFIG.assertSecret(RSA.secretKey("built-only"), RSA.secret);
    }

    @Test
    void cryptoSecretKeysHandlers() {
        testSecretKeysHandler(CRYPTO_AES_GCM_NO_PADDING);
    }

    private static void testSecretKeysHandler(SecretKeysHandler handler) {
        // system properties
        assertSecret(handler, "system");

        // MicroProfile Config configuration file
        assertSecret(handler, "mp");

        // keystore
        assertSecret(handler, "keystore");

        // custom source
        assertSecret(handler, "custom");

        // application properties
        // not default config source -> not automatically in manually built config
        var secretKey = handler.secretKey(APP_PROPERTIES);
        INJECTED_PROPERTIES.assertSecret(secretKey, handler.secret);
        INJECTED_CONFIG.assertSecret(secretKey, handler.secret);

        // custom config source factory
        secretKey = handler.secretKey("custom-factory");
        // configuration properties hidden from accidental exposure
        // unlocked
        BUILT_CONFIG.unlocked().assertSecret(secretKey, handler.secret);
        INJECTED_PROPERTIES.unlocked().assertSecret(secretKey, handler.secret);
        INJECTED_CONFIG.unlocked().assertSecret(secretKey, handler.secret);
        // locked
        BUILT_CONFIG.assertAccessDenied(secretKey);
        INJECTED_CONFIG.assertAccessDenied(secretKey);
        INJECTED_PROPERTIES.assertSecret(secretKey, handler.secret);
    }

    private static void assertSecret(SecretKeysHandler handler, String configSource) {
        var secretKey = handler.secretKey(configSource);
        BUILT_CONFIG.assertSecret(secretKey, handler.secret);
        INJECTED_PROPERTIES.assertSecret(secretKey, handler.secret);
        INJECTED_CONFIG.assertSecret(secretKey, handler.secret);
    }
}
