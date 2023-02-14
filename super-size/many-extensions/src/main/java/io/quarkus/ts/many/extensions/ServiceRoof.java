package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRoof {
    public String process(String name) {
        return "Roof - " + name + " - done";
    }
}
