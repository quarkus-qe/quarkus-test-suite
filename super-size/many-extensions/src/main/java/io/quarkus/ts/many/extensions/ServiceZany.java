package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceZany {
    public String process(String name) {
        return "Zany - " + name + " - done";
    }
}
