package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceShowy {
    public String process(String name) {
        return "Showy - " + name + " - done";
    }
}
