package io.quarkus.ts.funqy.knativeevents;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class ValidationResultRepository {

    private final Set<ValidationResult> repo = ConcurrentHashMap.newKeySet();

    void add(ValidationResult validationResult) {
        repo.add(validationResult);
    }

    Set<ValidationResult> getAll() {
        return repo;
    }

}
