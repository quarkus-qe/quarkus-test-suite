package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSteep {
    public String process(String name) {
        return "Steep - " + name + " - done";
    }
}
