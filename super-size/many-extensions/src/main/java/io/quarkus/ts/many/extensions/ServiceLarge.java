package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceLarge {
    public String process(String name) {
        return "Large - " + name + " - done";
    }
}
