package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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