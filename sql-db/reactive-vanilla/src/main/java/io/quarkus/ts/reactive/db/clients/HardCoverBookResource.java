package io.quarkus.ts.reactive.db.clients;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import io.quarkus.ts.reactive.db.clients.model.Book;
import io.quarkus.ts.reactive.db.clients.model.HardCoverBook;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

@Path("/book/{reactive-client}")
public class HardCoverBookResource extends CommonResource {

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll(@PathParam("reactive-client") Pool pool) {
        return HardCoverBook.findAll(pool)
                .onItem().transform(books -> Response.ok(Book.toJsonStringify(books)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id, @PathParam("reactive-client") Pool pool) {
        return HardCoverBook.findById(pool, id).onItem().transform(
                hardCoverBook -> Response.ok(hardCoverBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(HardCoverBook hardCoverBook, UriInfo uriInfo, @PathParam("reactive-client") Pool pool) {
        return hardCoverBook.save(pool)
                .onItem().transform(id -> fromId(id, uriInfo))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
