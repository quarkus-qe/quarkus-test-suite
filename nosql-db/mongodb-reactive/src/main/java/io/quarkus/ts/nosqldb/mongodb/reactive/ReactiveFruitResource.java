package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/reactive_fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReactiveFruitResource {

    @Inject
    ReactiveFruitService fruitService;

    @GET
    public Uni<List<Fruit>> getAllFruits() {
        return fruitService.listFruits();
    }

    @POST
    public Uni<List<Fruit>> addFruit(Fruit fruit) {
        return fruitService.addFruit(fruit)
                .onItem().ignore().andSwitchTo(this::getAllFruits);
    }
}
