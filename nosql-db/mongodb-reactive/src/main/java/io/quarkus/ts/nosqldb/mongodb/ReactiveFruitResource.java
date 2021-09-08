package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
