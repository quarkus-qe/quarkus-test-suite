package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceAmbitious {
    public String process(String name) {
        return "Ambitious - " + name + " - done";
    }
}
