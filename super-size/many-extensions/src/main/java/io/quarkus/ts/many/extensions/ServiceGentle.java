package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGentle {
    public String process(String name) {
        return "Gentle - " + name + " - done";
    }
}
