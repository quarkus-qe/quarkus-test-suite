package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

@ApplicationScoped
public class FruitBasketService extends AbstractMongoDao<FruitBasket> {

    private static final String FRUIT_BASKET_COLLECTION_NAME = "fruit_basket";

    public List<FruitBasket> listFruitBaskets() {
        return find(FRUIT_BASKET_COLLECTION_NAME, Filters.empty(), null, FruitBasket::fromDocument);
    }

    public List<FruitBasket> findFruitBasketsItemsOnly(String fruitBasketName) {
        return find(FRUIT_BASKET_COLLECTION_NAME, Filters.eq("name", fruitBasketName), Projections.include("items"),
                FruitBasket::fromDocument);
    }

    public void addFruitBasket(FruitBasket fruitBasket) {
        add(FRUIT_BASKET_COLLECTION_NAME, fruitBasket.toDocument());
    }
}
