package io.quarkus.ts.http.restclient.reactive.json;

import jakarta.ws.rs.PathParam;

public class IdBeanParam {
    private final String id;

    public IdBeanParam(String id) {
        this.id = id;
    }

    @PathParam("id")
    public String getId() {
        return id;
    }
}
