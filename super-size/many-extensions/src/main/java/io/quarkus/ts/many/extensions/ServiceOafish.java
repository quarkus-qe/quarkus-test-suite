package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceOafish {
    public String process(String name) {
        return "Oafish - " + name + " - done";
    }
}
