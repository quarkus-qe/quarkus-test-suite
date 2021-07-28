package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceTricky {
    public String process(String name) {
        return "Tricky - " + name + " - done";
    }
}
