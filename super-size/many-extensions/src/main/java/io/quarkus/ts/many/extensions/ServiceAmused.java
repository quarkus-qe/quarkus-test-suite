package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAmused {
    public String process(String name) {
        return "Amused - " + name + " - done";
    }
}
