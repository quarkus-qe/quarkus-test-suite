package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAmused {
    public String process(String name) {
        return "Amused - " + name + " - done";
    }
}
