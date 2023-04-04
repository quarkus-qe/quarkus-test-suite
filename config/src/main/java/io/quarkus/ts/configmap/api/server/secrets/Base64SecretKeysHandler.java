package io.quarkus.ts.configmap.api.server.secrets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

import io.smallrye.config.SecretKeysHandler;

public class Base64SecretKeysHandler implements SecretKeysHandler {
    @Override
    public String decode(String secretKey) {
        return new String(Base64.getUrlDecoder().decode(secretKey), UTF_8);
    }

    @Override
    public String getName() {
        return "custom-base64";
    }
}
