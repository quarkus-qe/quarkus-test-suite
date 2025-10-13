package io.quarkus.ts.security.https;

import jakarta.enterprise.event.Observes;

import io.quarkus.vertx.http.security.HttpSecurity;

public class HttpSecurityConfiguration {

    void configureSecuredEndpointsAuthorizationForUserRole(@Observes HttpSecurity httpSecurity) {
        // httpSecurity.path("/secured/*").roles("user");
        httpSecurity.path("/secured/*").authorization()
                .policy(identity -> identity.hasRole("user"));
    }

    void configureSecureMtlsEndpointsAuthorization(@Observes HttpSecurity httpSecurity) {
        httpSecurity.path("/secured/mtls").authenticated();
    }

}
