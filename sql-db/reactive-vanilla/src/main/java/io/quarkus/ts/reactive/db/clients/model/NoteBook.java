package io.quarkus.ts.reactive.db.clients.model;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.sqlclient.PropertyKind;

public class NoteBook extends Book {
    public static final String TABLE_NAME = "noteBook";
    private final static PropertyKind<Long> LAST_INSERTED_ID = PropertyKind.create("last-inserted-id", Long.class);

    public NoteBook() {
        // default constructor.
    }

    public NoteBook(Row row) {
        super(row);
    }

    public NoteBook(String title, String author) {
        super(title, author);
    }

    private static Multi<NoteBook> fromSet(RowSet<Row> rows) {
        return fromSet(rows, NoteBook::new);
    }

    public static Uni<List<NoteBook>> findAll(MySQLPool client) {
        return toList(client.query("SELECT * FROM " + TABLE_NAME).execute().onItem()
                .transformToMulti(NoteBook::fromSet));
    }

    public Uni<Long> save(MySQLPool client) {
        return client
                .preparedQuery(
                        "INSERT INTO " + TABLE_NAME + " (" + TITLE + ", " + AUTHOR + ") VALUES ('" + title + "', '" + author
                                + "')")
                .execute()
                .onItem().invoke(r -> client.query("SELECT LAST_INSERT_ID();"))
                .onItem().transform(id -> (Long) id.getDelegate().property(LAST_INSERTED_ID));
    }

    public static Uni<NoteBook> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, title, author FROM " + TABLE_NAME + " WHERE id = " + id).execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new NoteBook(iterator.next()) : null);
    }
}
