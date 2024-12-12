package io.quarkus.ts.spring.data.primitivetypes;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.ts.spring.data.primitivetypes.model.Book;

@ApplicationScoped
public class BookStore implements PanacheRepository<Book> {
    public Page<Book> findPaged(Pageable pageable) {
        List<Book> list = this.findAll().list();
        return new PageImpl<>(list, pageable, list.size());
    }
}
