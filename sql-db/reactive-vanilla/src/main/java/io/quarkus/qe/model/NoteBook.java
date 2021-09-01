package io.quarkus.qe.model;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.sqlclient.PropertyKind;

@RegisterForReflection
public class NoteBook extends Record {
    private final static PropertyKind<Long> LAST_INSERTED_ID = PropertyKind.create("last-inserted-id", Long.class);

    private static final String TITLE = "title";
    private String title;

    private static final String AUTHOR = "author";
    private String author;

    public NoteBook() {
        // default constructor.
    }

    public NoteBook(Long id, String title, String author) {
        super.setId(id);
        this.title = title;
        this.author = author;
    }

    public NoteBook(String title, String author) {
        this.title = title;
        this.author = author;
    }

    private static NoteBook from(Row row) {
        return new NoteBook(row.getLong(QUALIFIED_ID), row.getString(TITLE), row.getString(AUTHOR));
    }

    protected static Multi<NoteBook> fromSet(RowSet<Row> rows) {
        return Multi.createFrom().iterable(rows).onItem().transform(NoteBook::from);
    }

    public static Uni<List<NoteBook>> findAll(MySQLPool client) {
        return toList(client.query("SELECT * FROM noteBook").execute().onItem()
                .transformToMulti(NoteBook::fromSet));
    }

    public Uni<Long> save(MySQLPool client) {
        return client
                .preparedQuery(
                        "INSERT INTO noteBook (" + TITLE + ", " + AUTHOR + ") VALUES ('" + title + "', '" + author + "')")
                .execute()
                .onItem().invoke(r -> client.query("SELECT LAST_INSERT_ID();"))
                .onItem().transform(id -> (Long) id.getDelegate().property(LAST_INSERTED_ID));
    }

    public static Uni<NoteBook> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, title, author FROM noteBook WHERE id = " + id).execute()
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
