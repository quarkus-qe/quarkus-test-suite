package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceMindless {
    public String process(String name) {
        return "Mindless - " + name + " - done";
    }
}
