package io.quarkus.ts.spring.web.reactive.bootstrap.web;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.ts.spring.web.reactive.bootstrap.persistence.model.Book;
import io.quarkus.ts.spring.web.reactive.bootstrap.persistence.repo.BookRepository;
import io.quarkus.ts.spring.web.reactive.bootstrap.web.exception.BookIdMismatchException;
import io.quarkus.ts.spring.web.reactive.bootstrap.web.exception.BookNotFoundException;
import io.smallrye.mutiny.Uni;

@RestController
@RequestMapping("/api/books")
@Produces("application/json")
@Consumes("application/json")
public class BookController {

    private static final int FAIL_AFTER_SECONDS = 10;

    @Autowired
    BookRepository bookRepository;

    @WithSession
    @GetMapping
    public Uni<List<Book>> findAll() {
        return bookRepository.listAll().onItem()
                .ifNull().continueWith(Collections.emptyList())
                .ifNoItem().after(Duration.ofSeconds(FAIL_AFTER_SECONDS)).failWith(BookNotFoundException::new);
    }

    @WithSession
    @GetMapping("/title/{bookTitle}")
    public Uni<List<Book>> findByTitle(@PathVariable String bookTitle) {
        return bookRepository.findByTitle(bookTitle)
                .collect()
                .asList()
                .ifNoItem()
                .after(Duration.ofSeconds(FAIL_AFTER_SECONDS)).failWith(BookNotFoundException::new);
    }

    @WithSession
    @GetMapping("/{id}")
    public Uni<Book> findOne(@PathVariable long id) {
        return bookRepository.findById(id)
                .onItem().ifNull().failWith(BookNotFoundException::new)
                .ifNoItem().after(Duration.ofSeconds(FAIL_AFTER_SECONDS)).failWith(BookNotFoundException::new);
    }

    @WithSession
    @PostMapping
    public Uni<Response> create(@RequestBody Book book) {
        return Panache.withTransaction(() -> bookRepository.persist(book))
                .replaceWith(Response.ok((book)).status(CREATED)::build);
    }

    @DeleteMapping("/{id}")
    @WithTransaction
    public Uni<Response> delete(@PathVariable long id) {
        return bookRepository.deleteById(id)
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @WithTransaction
    public Uni<Book> update(@RequestBody Book book, @PathVariable long id) {
        if (book.getId() != id) {
            throw new BookIdMismatchException();
        }

        return bookRepository.findById(id)
                .onItem().ifNull().failWith(BookNotFoundException::new)
                .onItem().ifNotNull().invoke(item -> {
                    item.setAuthor(book.getAuthor());
                    item.setTitle(book.getTitle());
                    bookRepository.persist(item);
                });
    }
}
