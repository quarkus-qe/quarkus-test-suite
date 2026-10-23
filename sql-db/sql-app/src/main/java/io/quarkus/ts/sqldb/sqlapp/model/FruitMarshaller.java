package io.quarkus.ts.sqldb.sqlapp.model;

import io.quarkus.ts.sqldb.sqlapp.storage.FruitEntity;

public sealed interface FruitMarshaller extends Marshaller permits Fruit {

    static Fruit fromEntity(FruitEntity entity) {
        return new Fruit(entity.id, entity.name, entity.description);
    }

    static FruitEntity toEntity(Fruit fruit) {
        var entity = new FruitEntity();
        entity.name = fruit.name();
        entity.description = fruit.description();
        return entity;
    }
}