package io.quarkus.ts.http.restclient.reactive.exceptions;

public class MyCheckedException extends Exception {
    public MyCheckedException(String message) {
        super(message);
    }
}
