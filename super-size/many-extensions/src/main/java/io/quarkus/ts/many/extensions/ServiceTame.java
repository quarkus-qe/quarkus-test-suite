package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceTame {
    public String process(String name) {
        return "Tame - " + name + " - done";
    }
}
