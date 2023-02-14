package io.quarkus.ts.reactive.db.clients;

import java.net.URI;

import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.vertx.mutiny.sqlclient.Pool;

public class CommonResource {

    private static final Logger LOG = Logger.getLogger(CommonResource.class);

    // TODO remove this onStart once this issue is resolved: https://github.com/quarkusio/quarkus/issues/19766
    void setUpDB(Pool pool, String tableName) {
        pool.query("DROP TABLE IF EXISTS " + tableName).execute()
                .flatMap(r -> pool
                        .query("CREATE TABLE " + tableName
                                + " (id SERIAL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL)")
                        .execute())
                .flatMap(r -> pool.query("INSERT INTO " + tableName + " (title, author) VALUES ('Foundation', 'Isaac Asimov')")
                        .execute())
                .flatMap(r -> pool
                        .query("INSERT INTO " + tableName
                                + " (title, author) VALUES ('2001: A Space Odyssey', 'Arthur C. Clarke')")
                        .execute())
                .flatMap(r -> pool
                        .query("INSERT INTO " + tableName
                                + " (title, author) VALUES ('Stranger in a Strange Land', 'Robert A. Heinlein')")
                        .execute())
                .subscribe().with(item -> LOG.info(tableName + " table created"));
    }

    protected URI fromId(Long id, UriInfo uriInfo) {
        return URI.create(uriInfo.getPath() + "/" + id);
    }
}
