package io.quarkus.ts.security.form.authn.corsConfig;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.HttpSecurity;

public class SimpleCorsConfig {
    void configure(@Observes HttpSecurity httpSecurity) {
        httpSecurity.cors("http://localhost");
    }
}
