package io.quarkus.ts.security.form.authn;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.Form;
import io.quarkus.vertx.http.security.HttpSecurity;

public class FormAuthnProgrammaticConfiguration {

    void configureBasicAuthentication(@Observes HttpSecurity httpSecurity) {
        httpSecurity.mechanism(Form.builder()
                .httpOnlyCookie()
                .loginPage("login")
                .errorPage("error")
                .landingPage("landing")
                .build());
    }

}
