package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceTricky {
    public String process(String name) {
        return "Tricky - " + name + " - done";
    }
}
