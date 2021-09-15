package io.quarkus.ts.nosqldb.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@ApplicationScoped
public class FruitService implements FruitInterface {

    public static final String FRUIT_COLLECTION_NAME = "fruit";
    public static final String FRUIT_BASKET_COLLECTION_NAME = "fruit_basket";

    @Inject
    MongoClient mongoClient;

    public List<Fruit> listFruits() {
        return list(FRUIT_COLLECTION_NAME, Fruit::fromDocument);
    }

    public List<FruitBasket> listFruitBaskets() {
        return list(FRUIT_BASKET_COLLECTION_NAME, FruitBasket::fromDocument);
    }

    public void addFruit(Fruit fruit) {
        add(FRUIT_COLLECTION_NAME, fruit.toDocument());
    }

    public void addFruitBasket(FruitBasket fruitBasket) {
        add(FRUIT_BASKET_COLLECTION_NAME, fruitBasket.toDocument());
    }

    private <T> List<T> list(String collection, Function<Document, T> converter) {
        List<T> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = getCollection(collection).find().iterator()) {
            while (cursor.hasNext()) {
                list.add(converter.apply(cursor.next()));
            }
        }
        return list;
    }

    private void add(String collection, Document document) {
        getCollection(collection).insertOne(document);
    }

    private MongoCollection<Document> getCollection(String collection) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection);
    }
}
