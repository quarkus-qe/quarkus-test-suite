package io.quarkus.qe.model;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mssqlclient.MSSQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.sqlclient.PropertyKind;

public class HardCoverBook extends Book {
    public static final String TABLE_NAME = "hardCoverBook";
    private final static PropertyKind<Long> LAST_INSERTED_ID = PropertyKind.create("last-inserted-id", Long.class);

    public HardCoverBook() {
        // default constructor.
    }

    public HardCoverBook(Row row) {
        super(row);
    }

    public HardCoverBook(String title, String author) {
        super(title, author);
    }

    private static Multi<HardCoverBook> fromSet(RowSet<Row> rows) {
        return fromSet(rows, HardCoverBook::new);
    }

    public static Uni<List<HardCoverBook>> findAll(MSSQLPool client) {
        return toList(client.query("SELECT * FROM " + TABLE_NAME).execute().onItem()
                .transformToMulti(HardCoverBook::fromSet));
    }

    public Uni<Long> save(MSSQLPool client) {
        return client
                .preparedQuery(
                        "INSERT INTO " + TABLE_NAME + " (" + TITLE + ", " + AUTHOR + ") VALUES ('" + title + "', '" + author
                                + "')")
                .execute()
                .onItem().invoke(r -> client.query("SELECT SCOPE_IDENTITY() AS last-inserted-id"))
                .onItem().transform(id -> (Long) id.getDelegate().property(LAST_INSERTED_ID));
    }

    public static Uni<HardCoverBook> findById(MSSQLPool client, Long id) {
        return client.preparedQuery("SELECT id, title, author FROM " + TABLE_NAME + " WHERE id = " + id).execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new HardCoverBook(iterator.next()) : null);
    }

}
