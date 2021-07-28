package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSteep {
    public String process(String name) {
        return "Steep - " + name + " - done";
    }
}
