package io.quarkus.ts.spring.web.bootstrap.persistence.repo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.ts.spring.web.bootstrap.persistence.model.Book;

@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {
    public List<Book> findByTitle(String title) {
        return list("title", title);
    }
}
