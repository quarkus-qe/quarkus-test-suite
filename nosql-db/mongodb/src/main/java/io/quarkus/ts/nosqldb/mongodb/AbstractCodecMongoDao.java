package io.quarkus.ts.nosqldb.mongodb;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public abstract class AbstractCodecMongoDao<E> implements MongoDaoInterface {

    @Inject
    MongoClient mongoClient;

    protected List<E> find(String collection, Bson filter, Bson projection, Class<E> clazz) {
        List<E> list = new ArrayList<>();
        try (MongoCursor<E> cursor = getCollection(collection, clazz).find(filter).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        }
        return list;
    }

    protected void add(String collection, Class<E> clazz, E document) {
        getCollection(collection, clazz).insertOne(document);
    }

    private MongoCollection<E> getCollection(String collection, Class<E> clazz) {
        return mongoClient.getDatabase(FRUIT_DB_NAME).getCollection(collection, clazz);
    }
}
