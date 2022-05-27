package io.quarkus.ts.spring.web.reactive.boostrap.web.exception;

import javax.ws.rs.WebApplicationException;

public class BookIdMismatchException extends WebApplicationException {

    public BookIdMismatchException() {
        super();
    }

    public BookIdMismatchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BookIdMismatchException(final String message) {
        super(message);
    }

    public BookIdMismatchException(final Throwable cause) {
        super(cause);
    }
}
