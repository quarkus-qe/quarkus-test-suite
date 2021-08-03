package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceFlagrant {
    public String process(String name) {
        return "Flagrant - " + name + " - done";
    }
}
