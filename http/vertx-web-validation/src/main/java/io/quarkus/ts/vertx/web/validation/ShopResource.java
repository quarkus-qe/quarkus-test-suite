package io.quarkus.ts.vertx.web.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/shoppinglist")
@Produces(MediaType.APPLICATION_JSON)
public class ShopResource {

    private static List<ShoppingList> shoppingList = createSampleProductList();

    private static List<ShoppingList> createSampleProductList() {
        shoppingList = new ArrayList<>();
        shoppingList.add(new ShoppingList(UUID.randomUUID(), "ListName1", 25,
                new ArrayList<>(Arrays.asList("Carrots", "Water", "Cheese", "Beer"))));
        shoppingList.add(new ShoppingList(UUID.randomUUID(), "ListName2", 80,
                new ArrayList<>(Arrays.asList("Meat", "Wine", "Almonds", "Potatoes", "Cake"))));
        return shoppingList;
    }

    @GET
    public List<ShoppingList> get() {
        return shoppingList;
    }

}
