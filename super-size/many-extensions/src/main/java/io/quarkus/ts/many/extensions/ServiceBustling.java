package io.quarkus.ts.many.extensions;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceBustling {
    public String process(String name) {
        return "Bustling - " + name + " - done";
    }
}
