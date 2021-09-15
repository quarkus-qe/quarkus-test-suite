package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.Document;

import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ReactiveFruitService implements FruitInterface {

    public static final String REACTIVE_FRUIT_COLLECTION_NAME = "reactive_fruit";
    public static final String REACTIVE_FRUIT_BASKET_COLLECTION_NAME = "reactive_fruit_basket";

    @Inject
    ReactiveMongoClient mongoClient;

    public Uni<List<Fruit>> listFruits() {
        return list(REACTIVE_FRUIT_COLLECTION_NAME, Fruit::fromDocument);
    }

    public Uni<List<FruitBasket>> listFruitBaskets() {
        return list(REACTIVE_FRUIT_BASKET_COLLECTION_NAME, FruitBasket::fromDocument);
    }

    public Uni<Void> addFruit(Fruit fruit) {
        return add(REACTIVE_FRUIT_COLLECTION_NAME, fruit.toDocument());
    }

    public Uni<Void> addFruitBasket(FruitBasket fruitBasket) {
        return add(REACTIVE_FRUIT_BASKET_COLLECTION_NAME, fruitBasket.toDocument());
    }

    private <T> Uni<List<T>> list(String collection, Function<Document, T> converter) {
        return getCollection(collection).find().map(converter).collect().asList();
    }

    private Uni<Void> add(String collection, Document document) {
        return getCollection(collection).insertOne(document).onItem().ignore().andContinueWithNull();
    }

    private ReactiveMongoCollection<Document> getCollection(String collection) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection);
    }
}
