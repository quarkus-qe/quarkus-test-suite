package io.quarkus.ts.security.vertx.auth;

import java.util.Collections;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import io.quarkus.ts.security.vertx.config.AuthNConfig;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.mutiny.core.Vertx;

public class AuthN {

    @Inject
    AuthNConfig authNConf;

    @Inject
    Vertx vertx;

    @Produces
    JWTAuth jwtAuth() {
        return JWTAuth.create(vertx.getDelegate(), new JWTAuthOptions().setJWTOptions(getJwtOptions())
                .addPubSecKey(getPubSecKeyOptions()));
    }

    private PubSecKeyOptions getPubSecKeyOptions() {
        JsonObject authConfig = new JsonObject()
                .put("symmetric", false)
                .put("algorithm", authNConf.alg())
                .put("publicKey", Buffer.buffer(CertUtils.loadKey(authNConf.certPath())));

        return new PubSecKeyOptions(authConfig).setBuffer(authConfig.getBuffer("publicKey"));
    }

    private JWTOptions getJwtOptions() {
        return new JWTOptions()
                .setIgnoreExpiration(false)
                .setIssuer(authNConf.claims().iss())
                .setAudience(Collections.singletonList((authNConf.claims().aud())))
                .setSubject(authNConf.claims().sub())
                .setExpiresInMinutes(authNConf.liveSpan())
                .setAlgorithm(authNConf.alg());
    }

}
