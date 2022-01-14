package io.quarkus.ts.spring.web.boostrap.web.exception;

import javax.ws.rs.WebApplicationException;

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