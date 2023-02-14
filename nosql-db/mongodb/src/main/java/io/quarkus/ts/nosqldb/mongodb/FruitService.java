package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;

@ApplicationScoped
public class FruitService extends AbstractMongoDao<Fruit> {

    private static final String FRUIT_COLLECTION_NAME = "fruit";

    public List<Fruit> listFruits() {
        return find(FRUIT_COLLECTION_NAME, Filters.empty(), null, Fruit::fromDocument);
    }

    public void addFruit(Fruit fruit) {
        add(FRUIT_COLLECTION_NAME, fruit.toDocument());
    }
}
