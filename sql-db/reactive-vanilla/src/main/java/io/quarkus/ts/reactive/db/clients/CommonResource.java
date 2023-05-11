package io.quarkus.ts.reactive.db.clients;

import java.net.URI;

import jakarta.ws.rs.core.UriInfo;

public class CommonResource {

    protected URI fromId(Long id, UriInfo uriInfo) {
        return URI.create(uriInfo.getPath() + "/" + id);
    }
}
