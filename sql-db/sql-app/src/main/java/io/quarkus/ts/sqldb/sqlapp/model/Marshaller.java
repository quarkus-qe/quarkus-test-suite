package io.quarkus.ts.sqldb.sqlapp.model;

import io.vertx.core.json.JsonObject;

public sealed interface Marshaller permits FruitMarshaller {

    default String toJsonEncoded() {
        return toJson().encode();
    }

    default JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}