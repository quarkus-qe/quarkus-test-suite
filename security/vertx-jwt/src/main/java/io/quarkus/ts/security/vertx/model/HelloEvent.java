package io.quarkus.ts.security.vertx.model;

public class HelloEvent {

    final String message;

    public HelloEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
