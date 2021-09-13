package io.quarkus.qe.model;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

@RegisterForReflection
public class Book extends Record {

    private static final String TITLE = "title";
    private String title;

    private static final String AUTHOR = "author";
    private String author;

    public Book() {
        // default constructor.
    }

    public Book(Long id, String title, String author) {
        super.setId(id);
        this.title = title;
        this.author = author;
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    private static Book from(Row row) {
        return new Book(row.getLong(QUALIFIED_ID), row.getString(TITLE), row.getString(AUTHOR));
    }

    protected static Multi<Book> fromSet(RowSet<Row> rows) {
        return Multi.createFrom().iterable(rows).onItem().transform(Book::from);
    }

    public static Uni<List<Book>> findAll(PgPool client) {
        return toList(client.query("SELECT * FROM book").execute().onItem()
                .transformToMulti(Book::fromSet));
    }

    public Uni<Long> save(PgPool client) {
        return client.preparedQuery("INSERT INTO book (" + TITLE + ", " + AUTHOR + ") VALUES ($1, $2) RETURNING id")
                .execute(Tuple.of(this.title, this.author))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Book> findById(PgPool client, Long id) {
        return client.preparedQuery("SELECT id, title, author FROM book WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
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
