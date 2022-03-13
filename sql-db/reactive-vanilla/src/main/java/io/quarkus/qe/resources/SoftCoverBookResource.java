package io.quarkus.qe.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import io.quarkus.qe.model.Book;
import io.quarkus.qe.model.SoftCoverBook;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("/book/postgresql")
public class SoftCoverBookResource extends CommonResource {

    private static final Logger LOG = Logger.getLogger(SoftCoverBookResource.class);

    @Inject
    PgPool postgresql;

    // TODO remove this onStart once this issue is resolved: https://github.com/quarkusio/quarkus/issues/19766
    void onStart(@Observes StartupEvent ev) {
        setUpDB(postgresql, SoftCoverBook.TABLE_NAME);
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll() {
        return SoftCoverBook.findAll(postgresql)
                .onItem().transform(books -> Response.ok(Book.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return SoftCoverBook.findById(postgresql, id).onItem().transform(
                softCoverBook -> Response.ok(softCoverBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(SoftCoverBook softCoverBook, UriInfo uriInfo) {
        return softCoverBook.save(postgresql)
                .onItem().transform(id -> fromId(id, uriInfo))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
