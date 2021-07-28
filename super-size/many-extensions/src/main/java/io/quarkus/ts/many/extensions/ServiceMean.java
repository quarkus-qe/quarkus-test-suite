package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceMean {
    public String process(String name) {
        return "Mean - " + name + " - done";
    }
}
