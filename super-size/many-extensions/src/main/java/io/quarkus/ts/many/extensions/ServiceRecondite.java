package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRecondite {
    public String process(String name) {
        return "Recondite - " + name + " - done";
    }
}
