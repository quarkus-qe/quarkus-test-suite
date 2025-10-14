package io.quarkus.ts.security.jpa;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.HttpSecurity;

public class HttpAuthorizationConfiguration {

    void configurePathAuthorization(@Observes HttpSecurity httpSecurity) {
        httpSecurity
                .path("/api/users/*").roles("user")
                .path("/api/admin/*").roles("admin");
    }

}
