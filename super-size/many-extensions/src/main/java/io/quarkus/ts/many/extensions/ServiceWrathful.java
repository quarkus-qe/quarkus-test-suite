package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceWrathful {
    public String process(String name) {
        return "Wrathful - " + name + " - done";
    }
}
