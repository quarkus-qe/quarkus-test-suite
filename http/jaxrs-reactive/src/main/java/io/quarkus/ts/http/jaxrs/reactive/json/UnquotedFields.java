package io.quarkus.ts.http.jaxrs.reactive.json;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class UnquotedFields implements BiFunction<ObjectMapper, Type, ObjectWriter> {
    @Override
    public ObjectWriter apply(ObjectMapper objectMapper, Type type) {
        return objectMapper.writer().without(JsonWriteFeature.QUOTE_FIELD_NAMES);
    }
}
