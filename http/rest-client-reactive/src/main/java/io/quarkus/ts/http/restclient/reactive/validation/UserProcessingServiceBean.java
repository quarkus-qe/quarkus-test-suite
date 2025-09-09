package io.quarkus.ts.http.restclient.reactive.validation;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserProcessingServiceBean implements UserProcessingService {

    @Override
    public ValidatedUser process(ValidatedUser entity) {
        return entity;
    }
}
