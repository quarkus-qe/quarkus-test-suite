package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/codec_fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CodecFruitResource {

    @Inject
    CodecFruitService fruitService;

    @GET
    public List<Fruit> getAllFruits() {
        return fruitService.listFruits();
    }

    @POST
    public List<Fruit> addFruit(Fruit fruit) {
        fruitService.addFruit(fruit);
        return getAllFruits();
    }
}