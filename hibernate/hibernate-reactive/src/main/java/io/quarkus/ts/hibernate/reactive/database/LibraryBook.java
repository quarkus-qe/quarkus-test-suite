package io.quarkus.ts.hibernate.reactive.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Entity
@Table(name = "library_book")
public class LibraryBook extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Integer id;

    @NotNull
    @Size(min = 3)
    public String title;

    @Valid
    @ManyToOne
    @JoinColumn(name = "author_id")
    public LibraryAuthor author;
}
