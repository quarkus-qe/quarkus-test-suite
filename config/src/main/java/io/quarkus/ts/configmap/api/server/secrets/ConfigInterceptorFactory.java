package io.quarkus.ts.configmap.api.server.secrets;

import java.util.Set;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigSourceInterceptorFactory;
import io.smallrye.config.SecretKeysConfigSourceInterceptor;

public class ConfigInterceptorFactory implements ConfigSourceInterceptorFactory {
    @Override
    public ConfigSourceInterceptor getInterceptor(ConfigSourceInterceptorContext context) {
        return new SecretKeysConfigSourceInterceptor(Set.of("secret.password", "secrets.custom-factory-crypto-handler",
                "secrets.custom-factory-base64-handler", "secrets.custom-factory-sha256-handler"));
    }
}
