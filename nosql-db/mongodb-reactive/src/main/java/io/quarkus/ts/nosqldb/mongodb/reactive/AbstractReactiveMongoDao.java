package io.quarkus.ts.nosqldb.mongodb.reactive;

import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.conversions.Bson;

import io.quarkus.mongodb.FindOptions;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.smallrye.mutiny.Uni;

public class AbstractReactiveMongoDao<E> implements MongoDaoInterface {
    @Inject
    ReactiveMongoClient mongoClient;

    protected Uni<List<E>> list(String collection, Bson filter, Bson projection, Function<Document, E> converter) {
        return getCollection(collection).find(new FindOptions().filter(filter).projection(projection)).map(converter).collect()
                .asList();
    }

    protected Uni<Void> add(String collection, Document document) {
        return getCollection(collection).insertOne(document).onItem().ignore().andContinueWithNull();
    }

    private ReactiveMongoCollection<Document> getCollection(String collection) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection);
    }
}
