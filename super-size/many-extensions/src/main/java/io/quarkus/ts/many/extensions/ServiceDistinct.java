package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDistinct {
    public String process(String name) {
        return "Distinct - " + name + " - done";
    }
}
