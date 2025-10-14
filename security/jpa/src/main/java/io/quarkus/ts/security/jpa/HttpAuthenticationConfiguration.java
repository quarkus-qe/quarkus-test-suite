package io.quarkus.ts.security.jpa;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.HttpSecurity;

public class HttpAuthenticationConfiguration {

    void configureBasicAuthentication(@Observes HttpSecurity httpSecurity) {
        httpSecurity.basic();
    }

}
