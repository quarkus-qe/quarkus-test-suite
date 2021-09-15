package io.quarkus.ts.nosqldb.mongodb;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@ApplicationScoped
public class CodecFruitService implements FruitInterface {

    public static final String CODEC_FRUIT_COLLECTION_NAME = "codec_fruit";
    public static final String CODEC_FRUIT_BASKET_COLLECTION_NAME = "codec_fruit_basket";

    @Inject
    MongoClient mongoClient;

    public List<Fruit> listFruits() {
        return list(CODEC_FRUIT_COLLECTION_NAME, Fruit.class);
    }

    public List<FruitBasket> listFruitBaskets() {
        return list(CODEC_FRUIT_BASKET_COLLECTION_NAME, FruitBasket.class);
    }

    public void addFruit(Fruit fruit) {
        add(CODEC_FRUIT_COLLECTION_NAME, Fruit.class, fruit);
    }

    public void addFruitBasket(FruitBasket fruitBasket) {
        add(CODEC_FRUIT_BASKET_COLLECTION_NAME, FruitBasket.class, fruitBasket);
    }

    private <T> List<T> list(String collection, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try (MongoCursor<T> cursor = getCollection(collection, clazz).find().iterator()) {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        }
        return list;
    }

    private <T> void add(String collection, Class<T> clazz, T document) {
        getCollection(collection, clazz).insertOne(document);
    }

    private <T> MongoCollection<T> getCollection(String collection, Class<T> clazz) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection, clazz);
    }
}
