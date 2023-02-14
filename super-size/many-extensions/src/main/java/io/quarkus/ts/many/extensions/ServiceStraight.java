package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceStraight {
    public String process(String name) {
        return "Straight - " + name + " - done";
    }
}
