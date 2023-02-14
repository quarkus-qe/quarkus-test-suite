package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGlamorous {
    public String process(String name) {
        return "Glamorous - " + name + " - done";
    }
}
