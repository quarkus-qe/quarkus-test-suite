package io.quarkus.ts.http.restclient.reactive.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

import org.apache.commons.lang3.ClassUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Provider
// This code is taken from https://github.com/pravussum/quarkus-query-param-repro
public class JsonParamConverterProvider implements ParamConverterProvider {

    @Context
    private Providers providers;
    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        // only provide a JSON conversion for QueryParams (otherwise HeaderParams will get converted also)
        // Also use this ParamConverter if annotations are null in a best-effort manner since there is a path in
        // Resteasy client where query params are converted without the annotations being present in the context.
        // Otherwise toString() is called otherwise, which is definitely wrong
        // Once https://issues.redhat.com/browse/RESTEASY-3086 is fixed, we can strictly rely on the annotations param
        if (annotations != null && Arrays.stream(annotations)
                .noneMatch(a -> QueryParam.class.isAssignableFrom(a.annotationType()))) {
            return null;
        }

        // only provide JSON conversion for complex types
        if (!isGeneric(rawType, genericType) && isStringOrPrimitiveOrWrapper(rawType)) {
            return null;
        }

        return new JsonParamConverter<>(objectMapper, rawType, genericType);
    }

    private <T> boolean isGeneric(Class<T> rawType, Type genericType) {
        return genericType != null && !rawType.equals(genericType);
    }

    private <T> boolean isStringOrPrimitiveOrWrapper(Class<T> rawType) {
        try {
            return String.class.getName().equals(rawType.getTypeName()) ||
                    ClassUtils.isPrimitiveOrWrapper(Class.forName(rawType.getTypeName()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private class JsonParamConverter<T> implements ParamConverter<T> {

        private final ObjectMapper objectMapper;
        private final Class<T> rawType;
        private final JavaType genericType;

        public JsonParamConverter(ObjectMapper objectMapper, Class<T> rawType, Type genericType) {
            this.objectMapper = objectMapper;
            this.genericType = genericType != null ? TypeFactory.defaultInstance().constructType(genericType) : null;
            this.rawType = rawType;
        }

        @Override
        public T fromString(String value) {
            if (rawType.isAssignableFrom(String.class)) {
                //noinspection unchecked
                return (T) value;
            }
            try {
                return genericType != null ? objectMapper.readValue(value, genericType)
                        : objectMapper.readValue(value, rawType);
            } catch (JsonProcessingException e) {
                throw (new RuntimeException(e));
            }
        }

        @Override
        public String toString(T value) {
            // TODO remove once fixed in Resteasy
            // This is a hack until https://issues.redhat.com/browse/RESTEASY-3086 is resolved to prevent the JSON
            // encoding of the accept header parameter
            if (MediaType.class.equals(value.getClass())) {
                //noinspection RedundantCast
                return ((MediaType) value).toString();
            }
            if (rawType.isAssignableFrom(String.class)) {
                return (String) value;
            }
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw (new RuntimeException(e));
            }
        }
    }
}
