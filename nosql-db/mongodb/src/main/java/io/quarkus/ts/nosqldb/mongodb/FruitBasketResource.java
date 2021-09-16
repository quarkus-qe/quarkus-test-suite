package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/fruit_baskets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitBasketResource {

    @Inject
    FruitBasketService fruitBasketService;

    @GET
    public List<FruitBasket> getAllFruitBaskets() {
        return fruitBasketService.listFruitBaskets();
    }

    @GET
    @Path("/find-items/{fruitBasketName}")
    public List<FruitBasket> findFruitBasketAndGetItems(String fruitBasketName) {
        return fruitBasketService.findFruitBasketsItemsOnly(fruitBasketName);
    }

    @POST
    public List<FruitBasket> addFruitBasket(FruitBasket fruitBasket) {
        fruitBasketService.addFruitBasket(fruitBasket);
        return getAllFruitBaskets();
    }
}
