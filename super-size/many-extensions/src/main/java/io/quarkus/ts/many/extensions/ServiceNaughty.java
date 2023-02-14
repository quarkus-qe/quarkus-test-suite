package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceNaughty {
    public String process(String name) {
        return "Naughty - " + name + " - done";
    }
}
