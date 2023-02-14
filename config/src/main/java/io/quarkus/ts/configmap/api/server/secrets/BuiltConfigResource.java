package io.quarkus.ts.configmap.api.server.secrets;

import jakarta.ws.rs.Path;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

@Path("/built")
public class BuiltConfigResource extends SecretResource {

    private final SmallRyeConfig config;

    public BuiltConfigResource() {
        config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withSecretKeys("secret.ip")
                .build();
    }

    @Override
    public String getProperty(String key) {
        return config.getRawValue(key);
    }
}
