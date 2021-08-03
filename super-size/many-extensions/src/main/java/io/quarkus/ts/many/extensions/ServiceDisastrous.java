package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDisastrous {
    public String process(String name) {
        return "Disastrous - " + name + " - done";
    }
}
