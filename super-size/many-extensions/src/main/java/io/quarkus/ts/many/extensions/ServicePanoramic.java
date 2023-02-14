package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePanoramic {
    public String process(String name) {
        return "Panoramic - " + name + " - done";
    }
}
