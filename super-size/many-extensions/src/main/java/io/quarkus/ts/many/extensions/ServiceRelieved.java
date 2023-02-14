package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRelieved {
    public String process(String name) {
        return "Relieved - " + name + " - done";
    }
}
