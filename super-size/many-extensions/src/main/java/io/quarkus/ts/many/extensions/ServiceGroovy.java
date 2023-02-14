package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGroovy {
    public String process(String name) {
        return "Groovy - " + name + " - done";
    }
}
