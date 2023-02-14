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
import io.quarkus.ts.reactive.db.clients.model.HardCoverBook;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mssqlclient.MSSQLPool;
import io.vertx.mutiny.sqlclient.Pool;

@Path("/book/mssql")
public class HardCoverBookResource extends CommonResource {

    private static final Logger LOG = Logger.getLogger(HardCoverBookResource.class);

    @Inject
    @Named("mssql")
    MSSQLPool mssql;

    // TODO remove this onStart once this issue is resolved: https://github.com/quarkusio/quarkus/issues/19766
    void onStart(@Observes StartupEvent ev) {
        setUpDB(mssql, HardCoverBook.TABLE_NAME);
    }

    // Override for MSSQL specifics
    @Override
    void setUpDB(Pool pool, String tableName) {
        pool.query("DROP TABLE IF EXISTS " + tableName).execute()
                .flatMap(r -> pool
                        .query("CREATE TABLE " + tableName
                                + " (id int IDENTITY PRIMARY KEY, title varchar(max) NOT NULL, author varchar(max) NOT NULL)")
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

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll() {
        return HardCoverBook.findAll(mssql)
                .onItem().transform(books -> Response.ok(Book.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return HardCoverBook.findById(mssql, id).onItem().transform(
                hardCoverBook -> Response.ok(hardCoverBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(HardCoverBook hardCoverBook, UriInfo uriInfo) {
        return hardCoverBook.save(mssql)
                .onItem().transform(id -> fromId(id, uriInfo))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
