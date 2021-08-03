package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePossible {
    public String process(String name) {
        return "Possible - " + name + " - done";
    }
}
