package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestHeader;

@Path("/headersReflection")
public class HeadersReflectionResource {
    @GET
    @Path("/client")
    public String returnHeaders(@RestHeader("clientFilter") String header) {
        return header;
    }
}
