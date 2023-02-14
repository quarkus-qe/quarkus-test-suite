package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDisgusting {
    public String process(String name) {
        return "Disgusting - " + name + " - done";
    }
}
