package io.quarkus.ts.hibernate.reactive.database;

import java.util.List;

import jakarta.validation.Valid;

public class BookCollectionTypeUseValid {

    public List<@Valid LibraryBook> books;

    public BookCollectionTypeUseValid(List<LibraryBook> books) {
        this.books = books;
    }
}
