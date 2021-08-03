package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceLarge {
    public String process(String name) {
        return "Large - " + name + " - done";
    }
}
