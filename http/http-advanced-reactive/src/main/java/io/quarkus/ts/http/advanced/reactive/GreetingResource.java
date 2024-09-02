package io.quarkus.ts.http.advanced.reactive;

public class GreetingResource extends GreetingAbstractResource {
    @Override
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
