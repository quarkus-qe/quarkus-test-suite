package io.quarkus.ts.qe.services;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.logging.Log;

@ApplicationScoped
public class HelloService {

    public String greet(String name) {
        String message = String.format("Hello %s!", name);
        Log.info(message);
        return message;
    }
}
