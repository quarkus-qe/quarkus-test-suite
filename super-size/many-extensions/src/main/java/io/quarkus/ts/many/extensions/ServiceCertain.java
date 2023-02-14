package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceCertain {
    public String process(String name) {
        return "Certain - " + name + " - done";
    }
}
