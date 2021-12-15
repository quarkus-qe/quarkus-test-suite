package io.quarkus.ts.reactive.database;

import static javax.persistence.CascadeType.PERSIST;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;

import io.smallrye.mutiny.Multi;

@Entity
@Table(name = "authors")
public class Author {
    private static final int MAX_NAME_LENGTH = 10;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "authorIds")
    @GenericGenerator(name = "authorIds", strategy = "io.quarkus.ts.reactive.database.AuthorIdGenerator")
    private Integer id;

    @NotNull
    @Size(max = MAX_NAME_LENGTH)
    private String name;

    @OneToMany(mappedBy = "author", cascade = PERSIST, fetch = FetchType.EAGER)
    private List<Book> books = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public Multi<Book> getBooks() {
        return Multi.createFrom().iterable(books);
    }
}
