package io.quarkus.ts.security.jpa.reactive.security;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.Form;
import io.quarkus.vertx.http.security.HttpSecurity;

public class HttpSecurityConfig {

    void configure(@Observes HttpSecurity httpSecurity) {
        httpSecurity
                .basic()
                .mechanism(Form.create())
                .path("/crud").basic().permit()
                .get("/crud").basic().authorization()
                .policy((identity, ctx) -> identity.hasRole("user") || ctx.request().getHeader("trust-me-please") != null)
                .get("/crud/detail/*").basic().roles("admin");
    }

}
