package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceFlagrant {
    public String process(String name) {
        return "Flagrant - " + name + " - done";
    }
}
