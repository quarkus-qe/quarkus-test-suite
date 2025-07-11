package io.quarkus.ts.jakarta.data.db;

import java.util.List;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Table(name = "author")
@Entity
public class Author {

    public Author() {
    }

    public Author(long id, String firstName, String lastName, List<Book> books, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.books = books;
        this.id = id;
        this.address = address;
    }

    @Id
    private Long id;

    String firstName;

    String lastName;

    @OneToMany
    @JoinColumn(name = "bookId")
    List<Book> books;

    @Embedded
    Address address;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Book> getBooks() {
        return books;
    }

    public Address getAddress() {
        return address;
    }
}
