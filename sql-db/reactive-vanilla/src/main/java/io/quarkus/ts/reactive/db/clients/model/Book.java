package io.quarkus.ts.reactive.db.clients.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;

public class Book {

    protected static final String QUALIFIED_ID = "id";
    protected static final String TITLE = "title";
    protected static final String AUTHOR = "author";
    protected String title;
    protected String author;
    private Long id;

    public Book() {
        // default constructor.
    }

    public Book(Row row) {
        this.id = row.getLong(QUALIFIED_ID);
        this.title = row.getString(TITLE);
        this.author = row.getString(AUTHOR);
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    protected static <T> Multi<T> fromSet(RowSet<Row> rows, Function<Row, T> rowToInstance) {
        return Multi.createFrom().iterable(rows).onItem().transform(rowToInstance);
    }

    public String toJsonStringify() {
        return Json.encode(this);
    }

    public static String toJsonStringify(List<? extends Book> records) {
        return Json.encode(records);
    }

    public static <T> Uni<List<T>> toList(Multi<T> records) {
        return records.collect().in(ArrayList::new, List::add);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
