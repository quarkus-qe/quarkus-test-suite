package io.quarkus.ts.spring.web.reactive.boostrap.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.quarkus.arc.ArcUndeclaredThrowableException;
import io.quarkus.ts.spring.web.reactive.boostrap.web.exception.BookIdMismatchException;
import io.quarkus.ts.spring.web.reactive.boostrap.web.exception.BookNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            BookIdMismatchException.class,
            ArcUndeclaredThrowableException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }
}
