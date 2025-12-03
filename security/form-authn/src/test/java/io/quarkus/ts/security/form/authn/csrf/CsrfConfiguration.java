package io.quarkus.ts.security.form.authn.csrf;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.CSRF;
import io.quarkus.vertx.http.security.HttpSecurity;

public class CsrfConfiguration {
    public static final String FORM_FIELD_NAME = "quarkus-qe-token";
    public static final String COOKIE_NAME = "quarkus-qe-cookie";
    public static final String TOKEN_HEADER_NAME = "quarkus-qe-header";

    void configure(@Observes HttpSecurity httpSecurity) {
        httpSecurity.csrf(CSRF.builder()
                .formFieldName(FORM_FIELD_NAME)
                .cookieName(COOKIE_NAME)
                .tokenHeaderName(TOKEN_HEADER_NAME)
                .build());
    }
}
