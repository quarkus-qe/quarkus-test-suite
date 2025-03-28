package io.quarkus.ts.spring.web.reactive.bootstrap;

import static org.apache.commons.lang3.RandomStringUtils.insecure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.spring.web.reactive.bootstrap.persistence.model.Book;
import io.restassured.response.Response;

public abstract class AbstractSpringWebRestReactiveIT {

    private static final String API_ROOT = "/api/books";

    protected abstract RestService getApp();

    @Test
    public void whenGetNotExistBookById_thenNotFound() {
        final Response response = getApp().given().get(API_ROOT + "/" + insecure().nextNumeric(4));
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void whenInvalidBook_thenError() {
        final Book book = createRandomBook();
        book.setAuthor(null);

        final Response response = getApp().given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void whenGetCreatedBookById_thenOK() {
        final Book book = createRandomBook();
        final String location = createBookAsUri(book);

        final Response response = getApp().given().get(location);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(book.getTitle(), response.jsonPath()
                .get("title"));
    }

    @Test
    public void whenGetBooksByTitle_thenOK() {
        final Book book = createRandomBook();
        createBookAsUri(book);

        final Response response = getApp().given().get(API_ROOT + "/title/" + book.getTitle());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertFalse(response.as(List.class).isEmpty());
    }

    @Test
    public void whenCreateNewBook_thenCreated() {
        final Book book = createRandomBook();

        final Response response = getApp().given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
    }

    @Test
    public void whenGetAllBooks_thenOK() {
        final Response response = getApp().given().get(API_ROOT);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void whenUpdateCreatedBook_thenUpdated() {
        final Book book = createRandomBook();
        final String location = createBookAsUri(book);

        book.setId(Long.parseLong(location.split("api/books/")[1]));
        book.setAuthor("newAuthor");
        Response response = getApp().given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .put(location);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = getApp().given().get(location);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("newAuthor", response.jsonPath()
                .get("author"));
    }

    @Test
    public void whenDeleteCreatedBook_thenOk() {
        final Book book = createRandomBook();
        final String location = createBookAsUri(book);

        Response response = getApp().given().delete(location);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        response = getApp().given().get(location);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    // ===============================

    private Book createRandomBook() {
        final Book book = new Book();
        book.setTitle(insecure().nextAlphabetic(10));
        book.setAuthor(insecure().nextAlphabetic((15)));
        return book;
    }

    private String createBookAsUri(Book book) {
        final Response response = getApp().given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT);
        return API_ROOT + "/" + response.jsonPath()
                .get("id");
    }
}
