package io.quarkus.ts.hibernate.reactive.database;

import java.util.List;

import jakarta.validation.Valid;

public class BookCollectionLegacyValid {

    @Valid
    public List<LibraryBook> books;

    public BookCollectionLegacyValid(List<LibraryBook> books) {
        this.books = books;
    }
}
