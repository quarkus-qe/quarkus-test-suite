package io.quarkus.ts.configmap.api.server.config;

import static io.quarkus.ts.configmap.api.server.secrets.InjectedPropertiesResource.SECRETS_PREFIX;

import io.quarkus.ts.configmap.api.server.secrets.Sha256SecretKeysHandlerFactory;

public enum SecretKeysHandler {
    CRYPTO_AES_GCM_NO_PADDING("crypto-handler", "quarkus-qe-aes-gcm-no-padding"),
    BASE64("base64-handler", "quarkus-qe-base64"),
    SHA256("sha256-handler", Sha256SecretKeysHandlerFactory.SECRET),
    RSA("rsa-handler", "quarkus-qe-rsa");

    final String name;
    public final String secret;

    SecretKeysHandler(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    public String secretKey(String configSource) {
        return secretKey(configSource, "");
    }

    public String secretKey(String configSource, String profile) {
        boolean hasProfile = !profile.isEmpty();
        if (hasProfile) {
            profile += "-";
        }
        return SECRETS_PREFIX + profile + configSource + "-" + name;
    }
}
