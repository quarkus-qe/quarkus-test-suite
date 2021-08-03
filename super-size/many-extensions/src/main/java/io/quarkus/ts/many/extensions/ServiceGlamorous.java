package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceGlamorous {
    public String process(String name) {
        return "Glamorous - " + name + " - done";
    }
}
