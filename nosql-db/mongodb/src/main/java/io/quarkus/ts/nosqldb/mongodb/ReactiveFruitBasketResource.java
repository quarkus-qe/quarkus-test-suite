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

@Path("/reactive_fruit_baskets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReactiveFruitBasketResource {

    @Inject
    ReactiveFruitService fruitService;

    @GET
    public Uni<List<FruitBasket>> list() {
        return fruitService.listFruitBaskets();
    }

    @GET
    @Path("/find-items/{fruitBasketName}")
    public Uni<List<FruitBasket>> findItems(String fruitBasketName) {
        return fruitService.findFruitBasketsItemsOnly(fruitBasketName);
    }

    @POST
    public Uni<List<FruitBasket>> add(FruitBasket fruitBasket) {
        return fruitService.addFruitBasket(fruitBasket)
                .onItem().ignore().andSwitchTo(this::list);
    }
}
