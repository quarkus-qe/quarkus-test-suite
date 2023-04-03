package io.quarkus.ts.configmap.api.server.secrets;

import static io.quarkus.ts.configmap.api.server.secrets.RsaSecretKeysHandler.generateSecret;
import static java.lang.String.format;

import java.util.Map;

import jakarta.ws.rs.Path;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.common.MapBackedConfigSource;
import io.smallrye.config.source.keystore.KeyStoreConfigSourceFactory;

@Path("/built")
public class BuiltConfigResource extends SecretResource {

    private final SmallRyeConfig config;

    public BuiltConfigResource() {
        config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .addDiscoveredSecretKeysHandlers()
                .withSecretKeysHandlers(new RsaSecretKeysHandler())
                .withSecretKeys("secret.ip")
                .withSecretKeys("secrets.custom-factory-crypto-handler")
                .withSecretKeys("secrets.custom-factory-base64-handler")
                .withSecretKeys("secrets.custom-factory-sha256-handler")
                .withSources(withPropertyOnlyAvailableToBuiltConfig())
                .withSources(new KeyStoreConfigSourceFactory())
                .withSources(new CustomConfigSource())
                .withSources(new CustomConfigSourceFactory())
                .build();
    }

    private MapBackedConfigSource withPropertyOnlyAvailableToBuiltConfig() {
        // secret keys expression is validated at build time, and we can't define configuration properties for unknown handlers
        return new MapBackedConfigSource("built-only",
                Map.of("secrets.built-only-rsa-handler", format("${cus-rsa::%s}", generateSecret()))) {
        };
    }

    @Override
    public String getProperty(String key) {
        return config.getRawValue(key);
    }
}
