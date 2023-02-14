package io.quarkus.ts.nosqldb.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.inject.Inject;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public abstract class AbstractMongoDao<E> implements MongoDaoInterface {

    @Inject
    MongoClient mongoClient;

    protected List<E> find(String collection, Bson filter, Bson projection, Function<Document, E> converter) {
        List<E> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = getCollection(collection).find(filter).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                list.add(converter.apply(cursor.next()));
            }
        }
        return list;
    }

    protected void add(String collection, Document document) {
        getCollection(collection).insertOne(document);
    }

    private MongoCollection<Document> getCollection(String collection) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection);
    }
}
