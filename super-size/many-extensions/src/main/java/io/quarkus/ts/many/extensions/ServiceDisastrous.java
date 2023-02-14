package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDisastrous {
    public String process(String name) {
        return "Disastrous - " + name + " - done";
    }
}
