package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSunny {
    public String process(String name) {
        return "Sunny - " + name + " - done";
    }
}
