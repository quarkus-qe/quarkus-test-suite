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
import io.quarkus.ts.hibernate.reactive.multidbSources.thirdDatabase.Car;
import io.smallrye.mutiny.Uni;

@Path("/car")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarResource {
    @Inject
    @PersistenceUnit("cars")
    Mutiny.SessionFactory factory;

    @GET
    @Path("getAll")
    public Uni<Response> getAll() {
        return factory.withSession(session -> session.createNativeQuery("Select * from cars", Car.class)
                .getResultList()
                .map(cars -> Response.ok(cars).build()));
    }
}
