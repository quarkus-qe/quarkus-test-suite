package io.quarkus.ts.security.form.authn.corsConfig;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.CORS;
import io.quarkus.vertx.http.security.HttpSecurity;

public class FullCorsConfig {
    void configure(@Observes HttpSecurity httpSecurity) {
        httpSecurity.cors(CORS.builder()
                .origin("http://localhost")
                .method("POST")
                .build());
    }
}
