package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceFor {
    public String process(String name) {
        return "For - " + name + " - done";
    }
}
