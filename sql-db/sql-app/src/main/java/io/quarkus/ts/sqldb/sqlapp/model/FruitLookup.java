package io.quarkus.ts.sqldb.sqlapp.model;

import jakarta.ws.rs.core.Response;

public sealed interface FruitLookup permits FruitLookup.Found, FruitLookup.NotFound {

    int status();

    Object payload();

    record Found(Fruit fruit) implements FruitLookup {
        public int status() {
            return Response.Status.OK.getStatusCode();
        }

        public Object payload() {
            return fruit;
        }
    }

    record NotFound(Long id) implements FruitLookup {
        public int status() {
            return Response.Status.NOT_FOUND.getStatusCode();
        }

        public Object payload() {
            return "Fruit " + id + " not found";
        }
    }
}
