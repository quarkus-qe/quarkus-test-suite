package io.quarkus.ts.security.vertx.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "authN")
public interface AuthNConfig {
    String alg();

    String certPath();

    @WithName("token-live-span-min")
    int liveSpan();

    @WithName("jwt.claims")
    JwtClaims claims();

    interface JwtClaims {
        String iss();

        String sub();

        String aud();
    }
}
