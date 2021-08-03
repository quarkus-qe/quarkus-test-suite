package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceStraight {
    public String process(String name) {
        return "Straight - " + name + " - done";
    }
}
