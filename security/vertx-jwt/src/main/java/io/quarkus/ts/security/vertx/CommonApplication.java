package io.quarkus.ts.security.vertx;

public abstract class CommonApplication<T> {

    public static final String ADDRESS = "greeting";

    public abstract void consumeEventBusEvent(T event);
}
