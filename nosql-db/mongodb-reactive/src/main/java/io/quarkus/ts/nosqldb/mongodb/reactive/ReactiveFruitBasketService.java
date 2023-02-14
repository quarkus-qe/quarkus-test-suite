package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ReactiveFruitBasketService extends AbstractReactiveMongoDao<FruitBasket> {

    public static final String REACTIVE_FRUIT_BASKET_COLLECTION_NAME = "reactive_fruit_basket";

    public Uni<List<FruitBasket>> listFruitBaskets() {
        return list(REACTIVE_FRUIT_BASKET_COLLECTION_NAME, Filters.empty(), null, FruitBasket::fromDocument);
    }

    public Uni<Void> addFruitBasket(FruitBasket fruitBasket) {
        return add(REACTIVE_FRUIT_BASKET_COLLECTION_NAME, fruitBasket.toDocument());
    }

    public Uni<List<FruitBasket>> findFruitBasketsItemsOnly(String fruitBasketName) {
        return list(REACTIVE_FRUIT_BASKET_COLLECTION_NAME, Filters.eq("name", fruitBasketName), Projections.include("items"),
                FruitBasket::fromDocument);
    }
}
