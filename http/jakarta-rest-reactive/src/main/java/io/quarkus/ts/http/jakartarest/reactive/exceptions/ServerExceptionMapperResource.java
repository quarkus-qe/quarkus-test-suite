package io.quarkus.ts.http.jakartarest.reactive.exceptions;

import static io.quarkus.ts.http.jakartarest.reactive.exceptions.ServerExceptionMapperResource.EXCEPTION_MAPPER_PATH;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(EXCEPTION_MAPPER_PATH)
public class ServerExceptionMapperResource {

    public static final String EXCEPTION_MAPPER_PATH = "/server-exception-mapper";

    @GET
    @Path("/generic")
    @Produces(MediaType.TEXT_PLAIN)
    public String throwGeneric() {
        throw new GenericMapperException("generic mapper test");
    }

}
