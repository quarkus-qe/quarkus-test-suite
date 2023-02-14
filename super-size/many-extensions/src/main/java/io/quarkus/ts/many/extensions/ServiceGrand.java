package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGrand {
    public String process(String name) {
        return "Grand - " + name + " - done";
    }
}
