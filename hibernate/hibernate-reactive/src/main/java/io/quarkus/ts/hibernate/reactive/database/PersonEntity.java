package io.quarkus.ts.hibernate.reactive.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

@Entity
@Table(name = "person")
public class PersonEntity extends PanacheEntityBase {

    public static final int MAX_NAME_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @NotNull
    @Size(max = MAX_NAME_LENGTH, message = "Name must not exceed " + MAX_NAME_LENGTH + " characters")
    private String name;

    public static Uni<PersonEntity> create(String name) {
        PersonEntity person = new PersonEntity();
        person.name = name;
        return person.persistAndFlush();
    }

    public static Uni<Book> create(Integer author, String name) {
        Book book = new Book(name);
        book.setAuthor(author);
        return book.persistAndFlush();
    }
}
