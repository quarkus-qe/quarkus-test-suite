package io.quarkus.ts.http.advanced;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class LocalCustomContext {

    String value;

    public void set(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }

}