package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/codec_fruit_baskets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CodecFruitBasketResource {

    @Inject
    CodecFruitService fruitService;

    @GET
    public List<FruitBasket> list() {
        return fruitService.listFruitBaskets();
    }

    @GET
    @Path("/find-items/{fruitBasketName}")
    public List<FruitBasket> findItems(String fruitBasketName) {
        return fruitService.findFruitBasketsItemsOnly(fruitBasketName);
    }

    @POST
    public List<FruitBasket> add(FruitBasket fruitBasket) {
        fruitService.addFruitBasket(fruitBasket);
        return list();
    }
}