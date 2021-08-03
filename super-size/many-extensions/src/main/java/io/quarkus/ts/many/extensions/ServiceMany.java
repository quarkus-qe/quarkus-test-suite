package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceMany {
    public String process(String name) {
        return "Many - " + name + " - done";
    }
}
