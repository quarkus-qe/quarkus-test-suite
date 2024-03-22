package io.quarkus.ts.http.advanced.reactive;

import java.util.HashMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/compression")
public class Brotli4JResource {

    final static String DEFAULT_TEXT_PLAIN = "As you know, every little bit counts";

    @Inject
    Brotli4JRestMock brotli4JRestMock;

    @GET
    @Path("/brotli/json")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, Object> jsonHttpResponse() {
        return brotli4JRestMock.returnResponse();
    }

    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String textPlainHttpResponse() {
        return DEFAULT_TEXT_PLAIN;
    }

}