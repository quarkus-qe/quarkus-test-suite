package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePossible {
    public String process(String name) {
        return "Possible - " + name + " - done";
    }
}
