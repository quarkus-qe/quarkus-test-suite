package io.quarkus.ts.http.advanced.reactive;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/persons")
public class PersonResource {

    private final Set<Person> personSet = Collections.synchronizedSet(new LinkedHashSet<>());

    @Path("/process-json")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> processJson(List<Person> person) {
        personSet.addAll(person);
        return Uni.createFrom().item(() -> Response.status(201).entity(personSet).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllPersons() {
        if (personSet.isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        } else {
            return Uni.createFrom().item(Response.ok(personSet).build());
        }
    }
}
