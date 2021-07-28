package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceHushed {
    public String process(String name) {
        return "Hushed - " + name + " - done";
    }
}
