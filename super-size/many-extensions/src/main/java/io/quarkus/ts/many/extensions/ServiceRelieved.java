package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRelieved {
    public String process(String name) {
        return "Relieved - " + name + " - done";
    }
}
