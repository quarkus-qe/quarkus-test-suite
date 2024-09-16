package io.quarkus.ts.qe.services;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgeService {

    public String processAge(int age) {
        return "Processed age: " + age;
    }
}
