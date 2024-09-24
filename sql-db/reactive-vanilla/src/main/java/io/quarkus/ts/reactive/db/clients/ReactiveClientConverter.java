package io.quarkus.ts.reactive.db.clients;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.reactive.datasource.ReactiveDataSource;
import io.vertx.mutiny.mssqlclient.MSSQLPool;
import io.vertx.mutiny.sqlclient.Pool;

@Provider
public class ReactiveClientConverter implements ParamConverterProvider, ParamConverter<Pool> {

    @Inject
    @Named("mssql")
    MSSQLPool mssql;

    @Inject
    @ReactiveDataSource("mariadb")
    Pool mariadb;

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
        if (Pool.class == aClass) {
            return (ParamConverter<T>) this;
        }
        return null;
    }

    @Override
    public Pool fromString(String reactiveClient) {
        return switch (reactiveClient) {
            case "mariadb" -> mariadb;
            case "mssql" -> mssql;
            default -> throw new IllegalStateException("Unexpected reactive client: " + reactiveClient);
        };
    }

    @Override
    public String toString(Pool pool) {
        throw new UnsupportedOperationException("We only convert path param to a Pool");
    }
}
