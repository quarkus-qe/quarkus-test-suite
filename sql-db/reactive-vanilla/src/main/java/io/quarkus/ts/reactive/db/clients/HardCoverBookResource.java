package io.quarkus.ts.reactive.db.clients;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.inject.Inject;
import jakarta.inject.Named;
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
import io.vertx.mutiny.mssqlclient.MSSQLPool;

@Path("/book/mssql")
public class HardCoverBookResource extends CommonResource {

    @Inject
    @Named("mssql")
    MSSQLPool mssql;

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
