package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceOafish {
    public String process(String name) {
        return "Oafish - " + name + " - done";
    }
}
