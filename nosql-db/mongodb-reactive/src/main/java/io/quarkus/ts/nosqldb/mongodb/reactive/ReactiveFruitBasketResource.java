package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/reactive_fruit_baskets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReactiveFruitBasketResource {

    @Inject
    ReactiveFruitBasketService fruitBasketService;

    @GET
    public Uni<List<FruitBasket>> getAllFruitBaskets() {
        return fruitBasketService.listFruitBaskets();
    }

    @GET
    @Path("/find-items/{fruitBasketName}")
    public Uni<List<FruitBasket>> findFruitBasketAndGetItems(String fruitBasketName) {
        return fruitBasketService.findFruitBasketsItemsOnly(fruitBasketName);
    }

    @POST
    public Uni<List<FruitBasket>> addFruitBasket(FruitBasket fruitBasket) {
        return fruitBasketService.addFruitBasket(fruitBasket)
                .onItem().ignore().andSwitchTo(this::getAllFruitBaskets);
    }
}
