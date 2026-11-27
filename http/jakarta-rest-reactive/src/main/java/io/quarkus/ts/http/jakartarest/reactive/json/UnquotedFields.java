package io.quarkus.ts.http.jakartarest.reactive.json;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

public class UnquotedFields implements BiFunction<ObjectMapper, Type, ObjectWriter> {
    @Override
    public ObjectWriter apply(ObjectMapper objectMapper, Type type) {
        return objectMapper.writer().without(JsonWriteFeature.QUOTE_PROPERTY_NAMES);
    }
}
