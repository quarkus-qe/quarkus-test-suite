package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * Stores request headers from incoming server requests
 * and exposes an endpoint that returns these headers
 * It is used for testing purposes to verify if headers are correctly propagated.
 */
@Path("/fault/headers")
@ApplicationScoped
public class HeaderResource {

    private final List<Map<String, String>> headerList = new ArrayList<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStoredHeaders() {
        return Response.ok(headerList).build();
    }

    public void storeHeaders(MultivaluedMap<String, String> headers) {
        headerList.add(headers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))));
    }
}
