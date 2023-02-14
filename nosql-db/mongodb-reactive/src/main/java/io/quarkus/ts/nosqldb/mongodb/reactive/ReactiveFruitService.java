package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ReactiveFruitService extends AbstractReactiveMongoDao<Fruit> {

    public static final String REACTIVE_FRUIT_COLLECTION_NAME = "reactive_fruit";

    public Uni<List<Fruit>> listFruits() {
        return list(REACTIVE_FRUIT_COLLECTION_NAME, Filters.empty(), null, Fruit::fromDocument);
    }

    public Uni<Void> addFruit(Fruit fruit) {
        return add(REACTIVE_FRUIT_COLLECTION_NAME, fruit.toDocument());
    }
}
