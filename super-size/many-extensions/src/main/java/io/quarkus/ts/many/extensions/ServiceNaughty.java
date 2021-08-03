package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceNaughty {
    public String process(String name) {
        return "Naughty - " + name + " - done";
    }
}
