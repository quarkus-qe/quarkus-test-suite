package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePeachy {
    public String process(String name) {
        return "Peachy - " + name + " - done";
    }
}
