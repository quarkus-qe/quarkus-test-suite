package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAquatic {
    public String process(String name) {
        return "Aquatic - " + name + " - done";
    }
}
