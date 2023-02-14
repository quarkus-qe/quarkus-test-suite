package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePrecious {
    public String process(String name) {
        return "Precious - " + name + " - done";
    }
}
