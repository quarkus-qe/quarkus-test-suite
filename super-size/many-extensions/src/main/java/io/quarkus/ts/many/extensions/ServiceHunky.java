package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceHunky {
    public String process(String name) {
        return "Hunky - " + name + " - done";
    }
}
