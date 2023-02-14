package io.quarkus.ts.hibernate.reactive.database;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class AuthorRepository implements PanacheRepositoryBase<Author, Integer> {

    public Uni<Author> create(String name) {
        Author author = new Author();
        author.setName(name);
        return persistAndFlush(author);
    }

    public Uni<List<Author>> findByName(String name) {
        return find("name", name).list();
    }
}
