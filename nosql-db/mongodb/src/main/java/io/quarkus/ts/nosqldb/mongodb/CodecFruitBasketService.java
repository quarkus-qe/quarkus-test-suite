package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

@ApplicationScoped
public class CodecFruitBasketService extends AbstractCodecMongoDao<FruitBasket> {
    private static final String CODEC_FRUIT_BASKET_COLLECTION_NAME = "codec_fruit_basket";

    public List<FruitBasket> listFruitBaskets() {
        return find(CODEC_FRUIT_BASKET_COLLECTION_NAME, Filters.empty(), null, FruitBasket.class);
    }

    public List<FruitBasket> findFruitBasketsItemsOnly(String fruitBasketName) {
        return find(CODEC_FRUIT_BASKET_COLLECTION_NAME, Filters.eq("name", fruitBasketName), Projections.include("items"),
                FruitBasket.class);
    }

    public void addFruitBasket(FruitBasket fruitBasket) {
        add(CODEC_FRUIT_BASKET_COLLECTION_NAME, FruitBasket.class, fruitBasket);
    }

}
