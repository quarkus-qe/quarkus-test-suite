package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
