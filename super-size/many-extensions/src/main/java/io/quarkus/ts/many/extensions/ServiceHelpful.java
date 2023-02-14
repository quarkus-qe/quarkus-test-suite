package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceHelpful {
    public String process(String name) {
        return "Helpful - " + name + " - done";
    }
}
