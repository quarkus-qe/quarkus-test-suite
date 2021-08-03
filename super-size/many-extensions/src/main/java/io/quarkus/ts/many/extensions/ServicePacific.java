package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicePacific {
    public String process(String name) {
        return "Pacific - " + name + " - done";
    }
}
