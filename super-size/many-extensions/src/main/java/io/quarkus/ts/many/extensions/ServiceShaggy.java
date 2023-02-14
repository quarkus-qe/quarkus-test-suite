package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceShaggy {
    public String process(String name) {
        return "Shaggy - " + name + " - done";
    }
}
