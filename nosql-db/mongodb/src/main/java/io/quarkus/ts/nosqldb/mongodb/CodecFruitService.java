package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import com.mongodb.client.model.Filters;

@ApplicationScoped
public class CodecFruitService extends AbstractCodecMongoDao<Fruit> {

    private static final String CODEC_FRUIT_COLLECTION_NAME = "codec_fruit";

    public List<Fruit> listFruits() {
        return find(CODEC_FRUIT_COLLECTION_NAME, Filters.empty(), null, Fruit.class);
    }

    public void addFruit(Fruit fruit) {
        add(CODEC_FRUIT_COLLECTION_NAME, Fruit.class, fruit);
    }
}
