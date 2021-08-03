package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGroovy {
    public String process(String name) {
        return "Groovy - " + name + " - done";
    }
}
