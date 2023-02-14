package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceMany {
    public String process(String name) {
        return "Many - " + name + " - done";
    }
}
