package io.quarkus.ts.hibernate.reactive.multidbSources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.ts.hibernate.reactive.multidbSources.secondDatabase.Fruit;
import io.smallrye.mutiny.Uni;

@Path("/fruit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {
    @Inject
    @PersistenceUnit("fruits")
    Mutiny.SessionFactory factory;

    @GET
    @Path("getAll")
    public Uni<Response> getAll() {
        return factory.withSession(session -> session.createNativeQuery("Select * from fruits", Fruit.class)
                .getResultList()
                .map(fruits -> Response.ok(fruits).build()));
    }
}
