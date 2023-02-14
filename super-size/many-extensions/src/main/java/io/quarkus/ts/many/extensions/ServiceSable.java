package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSable {
    public String process(String name) {
        return "Sable - " + name + " - done";
    }
}
