package io.quarkus.ts.many.extensions;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceSqualid {
    public String process(String name) {
        return "Squalid - " + name + " - done";
    }
}
