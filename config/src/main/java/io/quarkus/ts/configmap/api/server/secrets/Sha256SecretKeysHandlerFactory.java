package io.quarkus.ts.configmap.api.server.secrets;

import io.quarkus.runtime.util.HashUtil;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.SecretKeysHandler;
import io.smallrye.config.SecretKeysHandlerFactory;

public class Sha256SecretKeysHandlerFactory implements SecretKeysHandlerFactory {

    public static final String SECRET = "quarkus-qe-sha256";

    @Override
    public SecretKeysHandler getSecretKeysHandler(ConfigSourceContext configSourceContext) {
        return new SecretKeysHandler() {
            @Override
            public String decode(String secretKey) {
                final boolean isSecret = HashUtil.sha256(SECRET).equals(secretKey);
                if (isSecret) {
                    return SECRET;
                }
                throw new IllegalArgumentException(String.format("Secret key '%s' can't be decrypted", secretKey));
            }

            @Override
            public String getName() {
                return "sha256";
            }
        };
    }

    @Override
    public String getName() {
        return Sha256SecretKeysHandlerFactory.class.getName();
    }
}
