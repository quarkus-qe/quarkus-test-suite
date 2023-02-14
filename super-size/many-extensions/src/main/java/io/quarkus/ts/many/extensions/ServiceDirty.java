package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDirty {
    public String process(String name) {
        return "Dirty - " + name + " - done";
    }
}
