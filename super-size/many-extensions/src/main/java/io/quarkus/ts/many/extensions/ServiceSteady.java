package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSteady {
    public String process(String name) {
        return "Steady - " + name + " - done";
    }
}
