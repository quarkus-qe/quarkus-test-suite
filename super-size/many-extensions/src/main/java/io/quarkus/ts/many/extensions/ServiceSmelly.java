package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSmelly {
    public String process(String name) {
        return "Smelly - " + name + " - done";
    }
}
