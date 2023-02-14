package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAdorable {
    public String process(String name) {
        return "Adorable - " + name + " - done";
    }
}
