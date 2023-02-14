package io.quarkus.ts.reactive.db.clients;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.ts.reactive.db.clients.model.Book;
import io.quarkus.ts.reactive.db.clients.model.NoteBook;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;

@Path("/book/mysql")
public class NoteBookResource extends CommonResource {

    private static final Logger LOG = Logger.getLogger(NoteBookResource.class);

    @Inject
    @Named("mysql")
    MySQLPool mysql;

    // TODO remove this onStart once this issue is resolved: https://github.com/quarkusio/quarkus/issues/19766
    void onStart(@Observes StartupEvent ev) {
        setUpDB(mysql, NoteBook.TABLE_NAME);
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll() {
        return NoteBook.findAll(mysql)
                .onItem().transform(books -> Response.ok(Book.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return NoteBook.findById(mysql, id).onItem().transform(noteBook -> Response.ok(noteBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(NoteBook noteBook, UriInfo uriInfo) {
        return noteBook.save(mysql)
                .onItem().transform(id -> fromId(id, uriInfo))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
