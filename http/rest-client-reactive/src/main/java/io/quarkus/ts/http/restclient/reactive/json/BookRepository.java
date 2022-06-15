package io.quarkus.ts.http.restclient.reactive.json;

import java.util.Map;

public class BookRepository {

    private static final Map<Integer, Book> REPO = Map.of(
            1, new Book("Title 1", "Author 1"),
            2, new Book("Title 2", "Author 2"),
            3, new Book("Title 3", "Author 3"),
            4, new Book("Title 4", "Author 4"),
            5, new Book("Title 5", "Author 5"),
            6, new Book("Title 6", "Author 6"));

    public static Book getById(Integer id) {
        return REPO.get(id);
    }

    public static int count() {
        return REPO.size();
    }

}
