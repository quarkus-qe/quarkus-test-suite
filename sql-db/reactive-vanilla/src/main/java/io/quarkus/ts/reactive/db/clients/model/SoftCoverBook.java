package io.quarkus.ts.reactive.db.clients.model;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class SoftCoverBook extends Book {
    public static final String TABLE_NAME = "softCoverBook";

    public SoftCoverBook() {
        // default constructor.
    }

    public SoftCoverBook(Row row) {
        super(row);
    }

    public SoftCoverBook(String title, String author) {
        super(title, author);
    }

    private static Multi<SoftCoverBook> fromSet(RowSet<Row> rows) {
        return fromSet(rows, SoftCoverBook::new);
    }

    public static Uni<List<SoftCoverBook>> findAll(PgPool client) {
        return toList(client.query("SELECT * FROM " + TABLE_NAME).execute().onItem()
                .transformToMulti(SoftCoverBook::fromSet));
    }

    public Uni<Long> save(PgPool client) {
        return client
                .preparedQuery("INSERT INTO " + TABLE_NAME + " (" + TITLE + ", " + AUTHOR + ") VALUES ($1, $2) RETURNING id")
                .execute(Tuple.of(this.title, this.author))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<SoftCoverBook> findById(PgPool client, Long id) {
        return client.preparedQuery("SELECT id, title, author FROM " + TABLE_NAME + " WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SoftCoverBook(iterator.next()) : null);
    }

}
