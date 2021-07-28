package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceTremendous {
    public String process(String name) {
        return "Tremendous - " + name + " - done";
    }
}
