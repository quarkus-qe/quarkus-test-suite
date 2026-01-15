package io.quarkus.ts.hibernate.reactive.multidbSources;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hibernate.SessionFactory;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.ts.hibernate.reactive.multidbSources.thirdDatabase.Car;

@Path("/car")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
/*
 * Same as CarResource, but doesn't use reactive APIs
 */
public class CarJDBCResource {
    @Inject
    @PersistenceUnit("cars")
    SessionFactory factory;

    @GET
    @Path("getAll")
    public Response getAll() {
        List<Car> resultList = factory.createEntityManager()
                .createNativeQuery("Select * from cars", Car.class)
                .getResultList();

        return Response.ok(resultList).build();
    }
}
