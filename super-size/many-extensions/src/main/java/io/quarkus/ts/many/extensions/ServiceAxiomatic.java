package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAxiomatic {
    public String process(String name) {
        return "Axiomatic - " + name + " - done";
    }
}
