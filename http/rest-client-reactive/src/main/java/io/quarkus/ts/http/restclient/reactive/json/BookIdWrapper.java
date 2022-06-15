package io.quarkus.ts.http.restclient.reactive.json;

public class BookIdWrapper {

    private final int id;

    public BookIdWrapper(int id) {
        this.id = id;
    }

    public BookIdWrapper(String id) {
        this.id = Integer.parseInt(id);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
