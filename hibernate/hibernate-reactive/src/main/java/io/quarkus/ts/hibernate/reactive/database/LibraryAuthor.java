package io.quarkus.ts.hibernate.reactive.database;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Entity
@Table(name = "library_author")
public class LibraryAuthor extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Integer id;

    @NotNull
    @Size(min = 3, max = 20)
    public String name;

    @Valid
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<LibraryBook> books = new ArrayList<>();
}
