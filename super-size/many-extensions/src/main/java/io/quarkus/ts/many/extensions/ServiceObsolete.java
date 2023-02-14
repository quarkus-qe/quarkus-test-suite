package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceObsolete {
    public String process(String name) {
        return "Obsolete - " + name + " - done";
    }
}
