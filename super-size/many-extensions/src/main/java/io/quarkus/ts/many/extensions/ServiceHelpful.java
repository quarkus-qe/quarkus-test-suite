package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceHelpful {
    public String process(String name) {
        return "Helpful - " + name + " - done";
    }
}
