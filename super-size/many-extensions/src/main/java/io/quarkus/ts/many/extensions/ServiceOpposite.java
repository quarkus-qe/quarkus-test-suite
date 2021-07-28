package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceOpposite {
    public String process(String name) {
        return "Opposite - " + name + " - done";
    }
}
