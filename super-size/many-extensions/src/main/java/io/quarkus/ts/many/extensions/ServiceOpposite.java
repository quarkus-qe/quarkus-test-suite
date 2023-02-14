package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceOpposite {
    public String process(String name) {
        return "Opposite - " + name + " - done";
    }
}
