package io.quarkus.ts.http.jakartarest.reactive.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Serves a {@link SequencedCollection} over REST: elements can be added to either end and the
 * collection can be read in its encounter order or reversed.
 */
@Path("/collections/sequenced")
@ApplicationScoped
public class SequencedCollectionResource {

    private final SequencedCollection<String> values = new ArrayList<>();

    @GET
    public synchronized SequencedCollection<String> get() {
        return List.copyOf(values);
    }

    @GET
    @Path("/reversed")
    public synchronized SequencedCollection<String> reversed() {
        return List.copyOf(values.reversed());
    }

    @POST
    @Path("/first")
    public synchronized Response addFirst(String value) {
        values.addFirst(value);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/last")
    public synchronized Response addLast(String value) {
        values.addLast(value);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    public synchronized Response clear() {
        values.clear();
        return Response.noContent().build();
    }
}
