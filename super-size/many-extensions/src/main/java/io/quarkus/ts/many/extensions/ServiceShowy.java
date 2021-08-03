package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceShowy {
    public String process(String name) {
        return "Showy - " + name + " - done";
    }
}
