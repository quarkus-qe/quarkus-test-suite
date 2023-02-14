package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceWrathful {
    public String process(String name) {
        return "Wrathful - " + name + " - done";
    }
}
