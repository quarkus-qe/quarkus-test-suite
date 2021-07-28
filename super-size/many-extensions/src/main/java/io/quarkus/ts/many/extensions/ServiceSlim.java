package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSlim {
    public String process(String name) {
        return "Slim - " + name + " - done";
    }
}
