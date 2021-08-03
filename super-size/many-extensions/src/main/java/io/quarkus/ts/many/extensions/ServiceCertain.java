package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceCertain {
    public String process(String name) {
        return "Certain - " + name + " - done";
    }
}
