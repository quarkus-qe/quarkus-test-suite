package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDirty {
    public String process(String name) {
        return "Dirty - " + name + " - done";
    }
}
