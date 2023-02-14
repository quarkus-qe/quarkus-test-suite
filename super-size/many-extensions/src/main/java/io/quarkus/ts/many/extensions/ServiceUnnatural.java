package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceUnnatural {
    public String process(String name) {
        return "Unnatural - " + name + " - done";
    }
}
