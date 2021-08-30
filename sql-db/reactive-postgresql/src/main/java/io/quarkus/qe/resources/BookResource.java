package io.quarkus.qe.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.quarkus.qe.model.Book;
import io.quarkus.qe.model.Record;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("/book")
public class BookResource {

    private static final Logger LOG = Logger.getLogger(BookResource.class);

    @Inject
    PgPool postgresql;

    // TODO remove this onStart once this issue is resolved: https://github.com/quarkusio/quarkus/issues/19766
    void onStart(@Observes StartupEvent ev) {
        postgresql.query("DROP TABLE IF EXISTS book").execute()
                .flatMap(r -> postgresql
                        .query("CREATE TABLE book (id SERIAL PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL)")
                        .execute())
                .flatMap(r -> postgresql.query("INSERT INTO book (title, author) VALUES ('Foundation', 'Isaac Asimov')")
                        .execute())
                .flatMap(r -> postgresql
                        .query("INSERT INTO book (title, author) VALUES ('2001: A Space Odyssey', 'Arthur C. Clarke')")
                        .execute())
                .flatMap(r -> postgresql
                        .query("INSERT INTO book (title, author) VALUES ('Stranger in a Strange Land', 'Robert A. Heinlein')")
                        .execute())
                .subscribe().with(item -> LOG.info("Book table created"));
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAllBooks() {
        return Book.findAllAsList(postgresql)
                .onItem().transform(books -> Response.ok(Record.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return Book.findById(postgresql, id).onItem().transform(book -> Response.ok(book.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(Book book) {
        return book.save(postgresql)
                .onItem().transform(id -> URI.create("/book/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
