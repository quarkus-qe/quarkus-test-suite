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
    FruitService fruitService;

    @GET
    public List<FruitBasket> list() {
        return fruitService.listFruitBaskets();
    }

    @POST
    public List<FruitBasket> add(FruitBasket fruitBasket) {
        fruitService.addFruitBasket(fruitBasket);
        return list();
    }
}
