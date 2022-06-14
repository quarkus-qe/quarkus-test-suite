package io.quarkus.ts.hibernate.reactive.http;

import io.quarkus.ts.hibernate.reactive.database.Book;
import io.smallrye.mutiny.Uni;

public class BookCountWrapper {

    private final Uni<Long> numOfBooks;

    private BookCountWrapper(Uni<Long> numOfBooks) {
        this.numOfBooks = numOfBooks;
    }

    public Uni<Long> getNumOfBooks() {
        return numOfBooks;
    }

    static BookCountWrapper valueOf(String bookName) {
        return new BookCountWrapper(Book.find("title = ?1", bookName).count());
    }

}
