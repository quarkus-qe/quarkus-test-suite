package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePrimo {
    public String process(String name) {
        return "Primo - " + name + " - done";
    }
}
