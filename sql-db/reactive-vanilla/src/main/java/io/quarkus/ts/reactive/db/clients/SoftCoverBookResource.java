package io.quarkus.ts.reactive.db.clients;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import io.quarkus.ts.reactive.db.clients.model.Book;
import io.quarkus.ts.reactive.db.clients.model.SoftCoverBook;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("/book/postgresql")
public class SoftCoverBookResource extends CommonResource {

    @Inject
    Instance<PgPool> postgresql;

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll() {
        return SoftCoverBook.findAll(postgresql.get())
                .onItem().transform(books -> Response.ok(Book.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return SoftCoverBook.findById(postgresql.get(), id).onItem().transform(
                softCoverBook -> Response.ok(softCoverBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(SoftCoverBook softCoverBook, UriInfo uriInfo) {
        return softCoverBook.save(postgresql.get())
                .onItem().transform(id -> fromId(id, uriInfo))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
