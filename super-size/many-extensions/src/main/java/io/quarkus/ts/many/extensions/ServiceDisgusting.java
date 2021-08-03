package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDisgusting {
    public String process(String name) {
        return "Disgusting - " + name + " - done";
    }
}
