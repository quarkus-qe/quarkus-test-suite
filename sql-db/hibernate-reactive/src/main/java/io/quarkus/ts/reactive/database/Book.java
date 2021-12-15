package io.quarkus.ts.reactive.database;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Entity
@Table(name = "books")
@NamedQuery(name = "find_by_title_prefix", query = "from Book where title like :prefix")
public class Book extends PanacheEntityBase {
    private static final int MAX_TITLE_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @NotNull
    @Size(max = MAX_TITLE_LENGTH)
    private String title;

    @NotNull
    private Integer author;

    @Convert(converter = ISBNConverter.class)
    private long isbn;

    public Book() {
    }

    public Book(String title) {
        this.title = title;
    }

    public static Multi<Book> all() {
        return streamAll();
    }

    public static Uni<Book> byId(Integer id) {
        return findById(id);
    }

    public static Uni<Book> create(Integer author, String name) {
        Book book = new Book(name);
        book.setAuthor(author);
        return book.persistAndFlush();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }

    public long getISBN() {
        return isbn;
    }

    public void setISBN(long isbn) {
        this.isbn = isbn;
    }
}
