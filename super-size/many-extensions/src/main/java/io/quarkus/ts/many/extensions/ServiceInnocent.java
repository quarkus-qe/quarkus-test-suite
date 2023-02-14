package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceInnocent {
    public String process(String name) {
        return "Innocent - " + name + " - done";
    }
}
