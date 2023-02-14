package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceFalse {
    public String process(String name) {
        return "False - " + name + " - done";
    }
}
