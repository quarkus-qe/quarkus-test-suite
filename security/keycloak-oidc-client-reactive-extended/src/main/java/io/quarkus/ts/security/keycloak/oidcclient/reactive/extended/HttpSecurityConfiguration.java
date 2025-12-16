package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.HttpSecurity;

public class HttpSecurityConfiguration {

    void configure(@Observes HttpSecurity httpSecurity) {
        httpSecurity
                .path("/generate-token/*", "/token/*", "/filter-messages/*", "/token-refresh-public/*",
                        "/filter-customization-messages/*", "/client-registration/*", "/method-public/*", "/method-secured/*")
                .permit()
                .path("/*").authenticated();
    }

    void configureLogout(@Observes HttpSecurity httpSecurity) {
        httpSecurity
                .get("/code-flow").permit()
                .path("/code-flow/logout").authenticated();
    }

}
