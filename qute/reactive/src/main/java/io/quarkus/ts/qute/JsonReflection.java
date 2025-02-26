package io.quarkus.ts.qute;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@RegisterForReflection(targets = { JsonObject.class, JsonArray.class })
public class JsonReflection {
    //workaround for https://github.com/quarkusio/quarkus/issues/46508
}
