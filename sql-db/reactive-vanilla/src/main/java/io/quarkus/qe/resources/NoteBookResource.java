package io.quarkus.qe.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.quarkus.qe.model.NoteBook;
import io.quarkus.qe.model.Record;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;

@Path("/notebook/mysql")
public class NoteBookResource extends CommonResource {

    private static final Logger LOG = Logger.getLogger(NoteBookResource.class);

    @Inject
    @Named("mysql")
    MySQLPool mysql;

    void onStart(@Observes StartupEvent ev) {
        setUpDB(mysql, "noteBook");
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Uni<Response> getAll() {
        return NoteBook.findAll(mysql)
                .onItem().transform(noteBook -> Response.ok(Record.toJsonStringify(noteBook)).build());
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Uni<Response> findById(@PathParam("id") Long id) {
        return NoteBook.findById(mysql, id).onItem().transform(noteBook -> Response.ok(noteBook.toJsonStringify()).build());
    }

    @POST
    public Uni<Response> create(NoteBook noteBook) {
        return noteBook.save(mysql)
                .onItem().transform(id -> URI.create("/noteBook/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }
}
