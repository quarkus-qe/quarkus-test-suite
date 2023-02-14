package io.quarkus.ts.spring.web.reactive.boostrap.web.exception;

import jakarta.ws.rs.WebApplicationException;

public class BookNotFoundException extends WebApplicationException {

    public BookNotFoundException() {
        super(404);
    }

    public BookNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BookNotFoundException(final String message) {
        super(message);
    }

    public BookNotFoundException(final Throwable cause) {
        super(cause);
    }
}