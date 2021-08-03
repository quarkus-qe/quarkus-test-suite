package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSmooth {
    public String process(String name) {
        return "Smooth - " + name + " - done";
    }
}
